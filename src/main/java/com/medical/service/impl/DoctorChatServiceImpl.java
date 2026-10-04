package com.medical.service.impl;

import com.medical.common.enums.ChatRoleEnum;
import com.medical.common.enums.SessionTypeEnum;
import com.medical.entity.dos.ChatMessage;
import com.medical.entity.dos.Report;
import com.medical.entity.dto.ChatMessageDTO;
import com.medical.entity.vos.ChatMessageVO;
import com.medical.mapper.ChatMessageMapper;
import com.medical.service.DoctorChatService;
import com.medical.service.LlmService;
import com.medical.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生对话业务层实现
 * @author wangda
 * @since 2026/09/30
 */
@Slf4j
@Service
public class DoctorChatServiceImpl implements DoctorChatService {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private LlmService llmService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendMessage(String reportId, String userMessage, String doctorId) {
        Report report = reportService.validateOwnership(reportId, doctorId);

        // 1. 组装系统提示词，带上当前报告的上下文
        String systemPrompt = buildSystemPrompt(report);

        // 2. 查询历史对话
        List<ChatMessage> historyEntities = chatMessageMapper.findHistory(
                reportId, SessionTypeEnum.DOCTOR_CHAT.code());
        List<ChatMessageDTO> history = historyEntities.stream()
                .map(m -> new ChatMessageDTO(m.getRole(), m.getContent()))
                .collect(Collectors.toList());

        // 3. 调用阿里千问大模型
        String reply = llmService.chat(systemPrompt, history, userMessage);

        // 4. 保存用户消息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setReportId(reportId);
        userMsg.setSessionType(SessionTypeEnum.DOCTOR_CHAT.code());
        userMsg.setRole(ChatRoleEnum.USER.code());
        userMsg.setContent(userMessage);
        userMsg.setCreateBy(doctorId);
        chatMessageMapper.save(userMsg);

        // 5. 保存AI回复内容
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setReportId(reportId);
        assistantMsg.setSessionType(SessionTypeEnum.DOCTOR_CHAT.code());
        assistantMsg.setRole(ChatRoleEnum.ASSISTANT.code());
        assistantMsg.setContent(reply);
        assistantMsg.setCreateBy(doctorId);
        chatMessageMapper.save(assistantMsg);

        log.info("医生对话完成，reportId：{}，用户消息内容：{}，AI回复内容：{}",
                reportId, userMessage, reply);

        return toVO(assistantMsg);
    }

    @Override
    public List<ChatMessageVO> getHistory(String reportId, String doctorId) {
        reportService.validateOwnership(reportId, doctorId);

        List<ChatMessage> messages = chatMessageMapper.findHistory(
                reportId, SessionTypeEnum.DOCTOR_CHAT.code());
        return messages.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearHistory(String reportId, String doctorId) {
        reportService.validateOwnership(reportId, doctorId);

        int rows = chatMessageMapper.softDeleteBySession(
                reportId, SessionTypeEnum.DOCTOR_CHAT.code());
        log.info("清空医生对话历史，reportId：{}，影响行数：{}", reportId, rows);
    }


    /**
     * 构造医生对话的系统提示词
     * @param report
     * @return
     */
    private String buildSystemPrompt(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an experienced radiologist AI assistant, helping doctors review and interpret ")
                .append("chest X-ray AI-generated reports.\n");
        sb.append("Answer the doctor's questions in concise, professional, and accurate English.\n");
        sb.append("You may discuss differential diagnoses, imaging findings, treatment suggestions, ")
                .append("and relevant literature.\n\n");
        sb.append("Current report context:\n");

        if (report.getImpression() != null && !report.getImpression().isEmpty()) {
            sb.append("[Impression] ").append(report.getImpression()).append("\n");
        }
        if (report.getReportContent() != null && !report.getReportContent().isEmpty()) {
            sb.append("[Report] ").append(report.getReportContent()).append("\n");
        }
        if (report.getFindingsKeywords() != null && !report.getFindingsKeywords().isEmpty()) {
            sb.append("[Detected findings] ").append(report.getFindingsKeywords()).append("\n");
        }
        if (report.getReportConfidence() != null) {
            sb.append("[AI confidence] ").append(report.getReportConfidence()).append("\n");
        }

        sb.append("\nPlease answer the doctor's questions based on the report context above.");
        return sb.toString();
    }

    private ChatMessageVO toVO(ChatMessage msg) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(msg, vo);
        return vo;
    }
}