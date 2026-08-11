package com.medical.common.enums;

/**
 * 报告状态枚举
 *
 * @author wangda
 * @since 2026/08/11
 */
public enum ReportStatusEnum {

    /**
     * 报告状态
     */
    DRAFT("草稿"),
    CONFIRMED("已确认"),
    SIGNED("已签发");

    private final String description;

    ReportStatusEnum(String description) {
        this.description = description;
    }

    public String description() {
        return this.description;
    }
}
