package com.medical.entity.vos;

import lombok.Data;

/**
 * 总览统计VO
 *
 * @author wangda
 * @since 2026/08/11
 */
@Data
public class OverviewVO {

    /** 总患者数 */
    private Long totalPatients;

    /** 总报告数 */
    private Long totalReports;

    /** 待审核数 */
    private Long draftCount;

    /** 已签发数 */
    private Long signedCount;

    /** AI准确率（百分比） */
    private Integer accuracy;
}
