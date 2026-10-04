package com.medical.entity.dos;

import com.medical.common.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对话消息表
 * @author wangda
 * @since 2026/09/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "chat_message")
public class ChatMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联报告ID */
    @Column(name = "report_id", nullable = false, length = 32)
    private String reportId;

    /** 会话类型：doctor_chat-医生对话/patient_chat-患者对话 */
    @Column(name = "session_type", nullable = false, length = 20)
    private String sessionType;

    /** 消息角色：user-用户提问/assistant-AI回复 */
    @Column(nullable = false, length = 20)
    private String role;

    /** 消息内容 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}