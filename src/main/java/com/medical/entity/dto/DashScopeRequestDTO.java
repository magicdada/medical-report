package com.medical.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 通义千问请求体DTO
 * @author wangda
 * @since 2026/09/30
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashScopeRequestDTO {

    /** 模型名称 */
    private String model;

    /** 输入内容 */
    private Input input;

    /** 请求参数 */
    private Parameters parameters;

    /**
     * 输入消息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Input {

        /** 对话消息列表 */
        private List<ChatMessageDTO> messages;
    }

    /**
     * 请求参数
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parameters {

        /** 返回格式：message表示对话格式 */
        @JsonProperty("result_format")
        private String resultFormat = "message";

        /** 采样温度 */
        private Double temperature;

        /** 最大生成token数 */
        @JsonProperty("max_tokens")
        private Integer maxTokens;
    }
}