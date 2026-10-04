package com.medical.service;

import com.medical.entity.dos.PatientChat;
import com.medical.entity.vos.ChatMessageVO;
import com.medical.entity.vos.PatientReportVO;
import java.util.List;

/**
 * 患者扫码对话服务层
 * @author wangda
 * @since 2026/10/01
 */
public interface PatientChatService {

    /**
     * 为指定报告生成一个扫码访问凭证
     *
     * @param reportId 报告ID
     * @param doctorId 操作医生ID
     * @return 凭证记录
     */
    PatientChat createAccessCode(String reportId, String doctorId);

    /**
     * 构造患者端完整访问URL(用于嵌入QR码)
     *
     * @param accessCode 访问码
     * @return 完整URL,如 http://192.168.1.XXX:3000/patient/chat/{accessCode}
     */
    String buildAccessUrl(String accessCode);

    /**
     * 校验访问码有效性,返回对应的报告ID
     *
     * @param accessCode 访问码
     * @return 关联的报告ID
     */
    String validateAccessCode(String accessCode);

    /**
     * 根据访问码获取患者端报告简化视图
     *
     * @param accessCode 访问码
     * @return 患者端报告VO
     */
    PatientReportVO getReportByAccessCode(String accessCode);

    /**
     * 患者发送消息,获取AI回复
     *
     * @param accessCode  访问码
     * @param userMessage 用户消息
     * @return AI回复
     */
    ChatMessageVO sendMessage(String accessCode, String userMessage);

    /**
     * 查询患者对话历史
     *
     * @param accessCode 访问码
     * @return 对话消息列表
     */
    List<ChatMessageVO> getHistory(String accessCode);
}