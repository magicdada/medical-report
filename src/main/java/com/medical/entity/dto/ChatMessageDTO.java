package com.medical.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息DTO
 * @author wangda
 * @since 2026/09/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {

    /** 角色：system/user/assistant */
    private String role;

    /** 消息内容 */
    private String content;
}