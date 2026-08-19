package com.medical.entity.vos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热力图VO（统一findings态和uncertain态的热力图结构）
 *
 * @author wangda
 * @since 2026/08/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeatmapVO {

    /** 视图：frontal/lateral */
    private String view;

    /** 热力图base64 */
    private String overlay;

    /** 关联关键词（findings态为病名，uncertain态为overall） */
    private String word;

    /** 类型：finding/overall */
    private String type;
}