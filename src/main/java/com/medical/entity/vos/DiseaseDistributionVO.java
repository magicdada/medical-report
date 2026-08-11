package com.medical.entity.vos;

import lombok.Data;

/**
 * 疾病分布VO
 *
 * @author wangda
 * @since 2026/08/11
 */
@Data
public class DiseaseDistributionVO {

    /** 疾病名称 */
    private String name;

    /** 数量 */
    private Integer count;

    /** 占比（百分比） */
    private Integer percent;
}
