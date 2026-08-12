package com.medical.entity.vos;

import lombok.Data;

/**
 * AI效率统计VO
 *
 * @author wangda
 * @since 2026/08/12
 */
@Data
public class EfficiencyVO {

    /** 使用AI前平均出报告时间（分钟） */
    private Integer avgTimeBefore;

    /** 使用AI后平均出报告时间（分钟） */
    private Integer avgTimeWithAi;

    /** 效率提升百分比 */
    private Integer improvementPercent;
}