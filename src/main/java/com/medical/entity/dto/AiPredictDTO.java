package com.medical.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * AI推理服务返回结果DTO
 *
 * @author wangda
 * @since 2026/08/17
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiPredictDTO {

    /** 报告内容 */
    private String report;

    /** 印象 */
    private String impression;

    /** 检测门控：normal/findings/uncertain */
    @JsonProperty("finding_status")
    private String findingStatus;

    /** 影像贡献度（nats） */
    @JsonProperty("image_contribution")
    private Double imageContribution;

    /** 顶层热力图（uncertain态） */
    private List<HeatmapItemDTO> heatmaps;

    /** 检测到的疾病 */
    private List<FindingDTO> findings;

    /** 状态 */
    private String status;

    /** 错误信息 */
    private String message;

    /**
     * 单个疾病发现
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FindingDTO {
        private String label;
        private String keyword;
        @JsonProperty("char_span")
        private List<Integer> charSpan;
        private Double confidence;
        /** 该疾病的热力图（findings态） */
        private List<HeatmapItemDTO> maps;
    }

    /**
     * 热力图项（findings态在 findings[].maps[]，uncertain态在顶层 heatmaps[]）
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeatmapItemDTO {
        private String view;
        private String image;
    }
}