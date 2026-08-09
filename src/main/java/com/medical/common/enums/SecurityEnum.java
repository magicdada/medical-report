package com.medical.common.enums;

/**
 * 安全相关常量
 *
 * @author wangda
 * @since 2026/08/08
 */
public enum SecurityEnum {

    /**
     * header中的token参数名
     */
    HEADER_TOKEN("accessToken"),

    /**
     * 存储在claims中的用户信息key
     */
    USER_CONTEXT("userContext");

    String value;

    SecurityEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}