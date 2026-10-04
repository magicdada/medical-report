package com.medical.common.enums;

/**
 * 对话会话类型枚举
 *
 * @author wangda
 * @since 2026/09/30
 */
public enum SessionTypeEnum {

    /**
     * 会话类型
     */
    DOCTOR_CHAT("doctor_chat", "医生对话"),
    PATIENT_CHAT("patient_chat", "患者对话");

    private final String code;
    private final String description;

    SessionTypeEnum(String code, String description) {
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