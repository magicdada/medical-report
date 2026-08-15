package com.medical.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报告对比统计DTO
 *
 * @author wangda
 * @since 2026/08/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonStatsDTO {

    /** 已对比报告总数 */
    private Long total;

    /** 未修改报告数量 */
    private Long unmodifiedCount;

    /** 微调报告数量（文本长度差异小于AI草稿的20%） */
    private Long minorEditsCount;
}