package com.medical.entity.vos;

import lombok.Data;

/**
 * AI对比统计VO
 *
 * @author wangda
 * @since 2026/08/11
 */
@Data
public class ComparisonStatsVO {

    /** 总对比数 */
    private Long total;

    /** 未修改占比 */
    private Integer unmodifiedPercent;

    /** 小幅修改占比 */
    private Integer minorEditsPercent;

    /** 大幅修改占比 */
    private Integer majorChangesPercent;
}
