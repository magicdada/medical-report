package com.medical.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报告统计概览DTO
 *
 * @author wangda
 * @since 2026/08/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportOverviewDTO {

    /** 报告总数 */
    private Long totalReports;

    /** 草稿数量 */
    private Long draftCount;

    /** 已签署/已确认数量 */
    private Long signedCount;

    /** 已对比报告数量（AI已生成且已填写最终报告） */
    private Long comparedCount;

    /** 未修改报告数量（完全采纳AI草稿） */
    private Long unmodifiedCount;
}