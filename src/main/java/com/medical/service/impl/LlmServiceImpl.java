package com.medical.service.impl;

import com.medical.common.ResultCode;
import com.medical.common.ServiceException;
import com.medical.common.properties.LlmProperties;
import com.medical.entity.dto.ChatMessageDTO;
import com.medical.entity.dto.DashScopeRequestDTO;
import com.medical.entity.dto.DashScopeResponseDTO;
import com.medical.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import com.medical.common.enums.ChatRoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 千问大模型调用业务层实现
 * @author wangda
 * @since 2026/09/30
 */
@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    /** 通义千问文本生成接口路径（DashScope API固定路径，不作为配置项）*/
    private static final String GENERATION_PATH = "/api/v1/services/aigc/text-generation/generation";

    @Autowired
    private LlmProperties llmProperties;

    private WebClient webClient;

    /**
     * 初始化WebClient
     */
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(llmProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + llmProperties.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String chat(String systemPrompt, List<ChatMessageDTO> history, String userMessage) {
        // 1. 组装消息列表
        List<ChatMessageDTO> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(new ChatMessageDTO(ChatRoleEnum.SYSTEM.code(), systemPrompt));
        }
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }
        messages.add(new ChatMessageDTO(ChatRoleEnum.USER.code(), userMessage));

        // 2. 组装请求体
        DashScopeRequestDTO.Input input = new DashScopeRequestDTO.Input();
        input.setMessages(messages);

        DashScopeRequestDTO.Parameters parameters = new DashScopeRequestDTO.Parameters();
        parameters.setTemperature(llmProperties.getTemperature());
        parameters.setMaxTokens(llmProperties.getMaxTokens());

        DashScopeRequestDTO body = new DashScopeRequestDTO();
        body.setModel(llmProperties.getModel());
        body.setInput(input);
        body.setParameters(parameters);

        log.info("调用通义千问，模型：{}，消息数：{}", llmProperties.getModel(), messages.size());

        try {
            // 3. 发送请求
            DashScopeResponseDTO response = webClient.post()
                    .uri(GENERATION_PATH)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(DashScopeResponseDTO.class)
                    .timeout(Duration.ofSeconds(llmProperties.getTimeoutSeconds()))
                    .block();

            // 4. 校验响应
            if (response == null) {
                log.error("通义千问返回为空");
                throw new ServiceException(ResultCode.LLM_RESPONSE_EMPTY);
            }
            if (response.getCode() != null && !response.getCode().isEmpty()) {
                log.error("通义千问返回错误，code：{}，msg：{}", response.getCode(), response.getMessage());
                throw new ServiceException(ResultCode.LLM_SERVICE_ERROR);
            }
            if (response.getOutput() == null
                    || response.getOutput().getChoices() == null
                    || response.getOutput().getChoices().isEmpty()) {
                log.error("通义千问返回结构异常");
                throw new ServiceException(ResultCode.LLM_RESPONSE_INVALID);
            }

            // 5. 提取回复内容
            String reply = response.getOutput()
                    .getChoices().get(0)
                    .getMessage()
                    .getContent();

            log.info("通义千问调用成功，回复内容：{}，长度：{}", reply.trim(), reply.length());
            return reply.trim();

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用通义千问失败:", e);
            throw new ServiceException(ResultCode.AI_SERVICE_ERROR);
        }
    }
}