package com.medical.service;

import com.medical.entity.vos.ChatMessageVO;
import java.util.List;

/**
 * 医生对话服务
 *
 * @author wangda
 * @since 2026/09/30
 */
public interface DoctorChatService {

    /**
     * 医生发送消息，获取AI回复
     *
     * @param reportId 报告ID
     * @param userMessage 用户消息
     * @param doctorId 当前医生ID（用于权限校验）
     * @return AI回复消息VO
     */
    ChatMessageVO sendMessage(String reportId, String userMessage, String doctorId);

    /**
     * 查询医生对话历史
     *
     * @param reportId 报告ID
     * @param doctorId 当前医生ID（用于权限校验）
     * @return 对话消息列表（按时间升序）
     */
    List<ChatMessageVO> getHistory(String reportId, String doctorId);

    /**
     * 清空医生对话历史
     *
     * @param reportId 报告ID
     * @param doctorId 当前医生ID（用于权限校验）
     */
    void clearHistory(String reportId, String doctorId);
}