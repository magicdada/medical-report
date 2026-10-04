package com.medical.common.enums;

/**
 * 对话角色枚举（遵循通义千问/OpenAI大模型协议）
 *
 * @author wangda
 * @since 2026/09/30
 */
public enum ChatRoleEnum {

    /**
     * 对话角色
     */
    SYSTEM("system", "系统提示词"),
    USER("user", "用户"),
    ASSISTANT("assistant", "AI助手");

    private final String code;
    private final String description;

    ChatRoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return this.code;
    }

    public String description() {
        return this.description;
    }
}