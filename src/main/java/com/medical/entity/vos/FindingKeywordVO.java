package com.medical.entity.vos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 疾病关键词VO（存储到数据库，不含热力图图片数据）
 *
 * @author wangda
 * @since 2026/08/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindingKeywordVO {

    /** 疾病标签 */
    private String label;

    /** 触发关键词 */
    private String keyword;

    /** 模型置信度 */
    private Double confidence;
}