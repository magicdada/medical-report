package com.medical.service;

import com.medical.entity.dto.ChatMessageDTO;
import java.util.List;

/**
 * 千问大模型调用业务层
 * @author wangda
 * @since 2026/09/30
 */
public interface LlmService {

    /**
     * 调用大模型生成回复
     * @param systemPrompt 系统提示词
     * @param history 历史对话
     * @param userMessage 用户消息
     * @return AI回复内容
     */
    String chat(String systemPrompt, List<ChatMessageDTO> history, String userMessage);
}