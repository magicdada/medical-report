package com.medical.entity.vos;

import lombok.Data;

/**
 * 月度报告量VO
 *
 * @author wangda
 * @since 2026/08/11
 */
@Data
public class MonthlyVolumeVO {

    /** 月份标签 */
    private String month;

    /** 报告数量 */
    private Long count;
}
