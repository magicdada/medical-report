package com.medical.service.impl;

import com.medical.common.ResultCode;
import com.medical.common.ServiceException;
import com.medical.common.enums.ChatRoleEnum;
import com.medical.common.enums.SessionTypeEnum;
import com.medical.common.properties.PatientChatProperties;
import com.medical.common.util.UuidUtil;
import com.medical.entity.dos.ChatMessage;
import com.medical.entity.dos.Patient;
import com.medical.entity.dos.PatientChat;
import com.medical.entity.dos.Report;
import com.medical.entity.dto.ChatMessageDTO;
import com.medical.entity.vos.ChatMessageVO;
import com.medical.entity.vos.PatientReportVO;
import com.medical.mapper.ChatMessageMapper;
import com.medical.mapper.PatientChatMapper;
import com.medical.mapper.PatientMapper;
import com.medical.mapper.ReportMapper;
import com.medical.service.LlmService;
import com.medical.service.PatientChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 患者扫码对话业务层实现
 * @author wangda
 * @since 2026/10/01
 */
@Slf4j
@Service
public class PatientChatServiceImpl implements PatientChatService {

    /**
     * 一天的毫秒数
     */
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;

    /**
     * 患者端前端路径
     */
    private static final String PATIENT_CHAT_PATH = "/patient/chat/";

    /**
     * 患者端免责声明
     */
    private static final String PATIENT_DISCLAIMER = "This report is for reference only. For medical decisions, please consult your doctor.";

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private PatientChatMapper patientChatMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private LlmService llmService;

    @Autowired
    private PatientChatProperties patientChatProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientChat createAccessCode(String reportId, String doctorId) {
        // 生成唯一访问码
        String accessCode = UuidUtil.getUUID();

        // 构造凭证记录
        PatientChat patientChat = new PatientChat();
        patientChat.setReportId(reportId);
        patientChat.setAccessCode(accessCode);
        patientChat.setExpireTime(new Date(System.currentTimeMillis() + patientChatProperties.getExpireDays() * ONE_DAY_MILLIS));
        patientChat.setRevoked(false);
        patientChat.setCreateBy(doctorId);
        patientChatMapper.save(patientChat);

        log.info("患者访问码生成成功,reportId:{},accessCode:{}", reportId, accessCode);
        return patientChat;
    }

    @Override
    public String buildAccessUrl(String accessCode) {
        return patientChatProperties.getFrontendBaseUrl() + PATIENT_CHAT_PATH + accessCode;
    }

    @Override
    public String validateAccessCode(String accessCode) {
        PatientChat patientChat = patientChatMapper.findByAccessCode(accessCode);
        if (patientChat == null) {
            throw new ServiceException(ResultCode.PATIENT_CHAT_INVALID);
        }
        if (Boolean.TRUE.equals(patientChat.getRevoked())) {
            throw new ServiceException(ResultCode.PATIENT_CHAT_REVOKED);
        }
        if (patientChat.getExpireTime().before(new Date())) {
            throw new ServiceException(ResultCode.PATIENT_CHAT_EXPIRED);
        }

        return patientChat.getReportId();
    }


    @Override
    public PatientReportVO getReportByAccessCode(String accessCode) {
        String reportId = validateAccessCode(accessCode);
        Report report = reportMapper.findById(reportId).orElse(null);

        if (report == null) {
            throw new ServiceException(ResultCode.REPORT_NOT_EXIST);
        }
        Patient patient = patientMapper.findById(report.getPatientId()).orElse(null);
        if (patient == null) {
            throw new ServiceException(ResultCode.PATIENT_NOT_EXIST);
        }

        PatientReportVO vo = new PatientReportVO();
        vo.setReportId(report.getId());
        vo.setPatientName(patient.getName());
        vo.setImpression(report.getImpression());
        vo.setReportDate(report.getCreateTime());
        vo.setDisclaimer(PATIENT_DISCLAIMER);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendMessage(String accessCode, String userMessage) {
        // 校验访问码,拿到报告Id
        String reportId = validateAccessCode(accessCode);
        Report report = reportMapper.findById(reportId).orElse(null);
        if (report == null) {
            throw new ServiceException(ResultCode.REPORT_NOT_EXIST);
        }

        // 1. 组装系统提示词，带上当前报告的上下文
        String systemPrompt = buildPatientSystemPrompt(report);

        // 2. 查询历史对话
        List<ChatMessage> historyEntities = chatMessageMapper.findHistory(
                reportId, SessionTypeEnum.PATIENT_CHAT.code());
        List<ChatMessageDTO> history = historyEntities.stream()
                .map(m -> new ChatMessageDTO(m.getRole(), m.getContent()))
                .collect(Collectors.toList());

        // 3. 调用阿里千问大模型
        String reply = llmService.chat(systemPrompt, history, userMessage);

        // 4. 保存用户信息
        ChatMessage userMsg = new ChatMessage();
        userMsg.setReportId(reportId);
        userMsg.setSessionType(SessionTypeEnum.PATIENT_CHAT.code());
        userMsg.setRole(ChatRoleEnum.USER.code());
        userMsg.setContent(userMessage);
        userMsg.setCreateBy("patient");
        chatMessageMapper.save(userMsg);

        // 5. 保存AI回复内容
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setReportId(reportId);
        assistantMsg.setSessionType(SessionTypeEnum.PATIENT_CHAT.code());
        assistantMsg.setRole(ChatRoleEnum.ASSISTANT.code());
        assistantMsg.setContent(reply);
        assistantMsg.setCreateBy("patient");
        chatMessageMapper.save(assistantMsg);

        log.info("患者对话完成,reportId:{},用户消息内容:{},AI回复内容:{}",
                reportId, userMessage, reply);

        return toVO(assistantMsg);
    }

    @Override
    public List<ChatMessageVO> getHistory(String accessCode) {
        String reportId = validateAccessCode(accessCode);
        List<ChatMessage> messages = chatMessageMapper.findHistory(
                reportId, SessionTypeEnum.PATIENT_CHAT.code());
        return messages.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 构造患者端系统提示词
     */
    private String buildPatientSystemPrompt(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a friendly health assistant helping a patient understand their chest X-ray report. ")
                .append("Follow these rules strictly:\n")
                .append("1. Use simple, everyday language. Avoid medical jargon when possible. If you must use a technical term, explain it in plain words.\n")
                .append("2. Be warm, empathetic, and reassuring in tone.\n")
                .append("3. DO NOT give specific treatment recommendations, drug names, or dosages.\n")
                .append("4. DO NOT make a definitive diagnosis. The AI report is for reference only.\n")
                .append("5. For any serious concern, ALWAYS recommend the patient consult their doctor.\n")
                .append("6. Keep answers concise (3-5 sentences) unless the patient asks for details.\n\n");

        sb.append("The patient's report summary:\n");
        if (report.getImpression() != null && !report.getImpression().isEmpty()) {
            sb.append("[Diagnosis] ").append(report.getImpression()).append("\n");
        }

        sb.append("\nAnswer the patient's questions based on this report. ")
                .append("At the end of your response, briefly remind them this is not a substitute for professional medical advice.");

        return sb.toString();
    }

    /**
     * Entity 转 VO
     */
    private ChatMessageVO toVO(ChatMessage msg) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(msg, vo);
        return vo;
    }
}