package com.medical.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * AI推理服务返回结果DTO
 * @author wangda
 * @since 2026/08/17
 */
@Data
public class AiPredictDTO {

    /** 报告内容 */
    private String report;

    /** 印象 */
    private String impression;

    /** 检测门控：normal/findings/uncertain */
    @JsonProperty("finding_status")
    private String findingStatus;

    /** 检测到的疾病关键词 */
    private List<FindingDTO> findings;

    @JsonProperty("image_contribution")
    private Double imageContribution;

    /** 多视图热力图 */
    private List<HeatmapDTO> heatmaps;

    /** 状态 */
    private String status;

    @Data
    public static class FindingDTO {
        private String label;
        private String keyword;
        @JsonProperty("char_span")
        private List<Integer> charSpan;
        private Double confidence;
        private List<FindingMapDTO> maps;
    }

    @Data
    public static class FindingMapDTO {
        private String view;
        private String image;
    }

//    @Data
//    public static class ConfidenceDTO {
//        @JsonProperty("report_confidence")
//        private Double report_confidence;
//    }

    @Data
    public static class HeatmapDTO {
        private String view;
        private String overlay;
        @JsonProperty("word_maps")
        private List<WordMapDTO> wordMaps;
    }

    @Data
    public static class WordMapDTO {
        private String word;
        private String overlay;
    }
}