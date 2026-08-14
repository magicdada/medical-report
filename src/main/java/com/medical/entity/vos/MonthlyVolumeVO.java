package com.medical.entity.vos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度报告量VO
 *
 * @author wangda
 * @since 2026/08/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyVolumeVO {

    /** 月份标签 */
    private String month;

    /** 报告数量 */
    private Long count;
}
