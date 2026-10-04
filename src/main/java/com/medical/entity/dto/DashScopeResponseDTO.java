package com.medical.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 通义千问响应体DTO
 * @author wangda
 * @since 2026/09/30
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashScopeResponseDTO {

    /** 错误码（正常时为空）*/
    private String code;

    /** 错误信息 */
    private String message;

    /** 请求ID */
    @JsonProperty("request_id")
    private String requestId;

    /** 输出内容 */
    private Output output;

    /** token使用统计 */
    private Usage usage;

    /**
     * 输出内容
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {

        /** 生成结果候选列表 */
        private List<Choice> choices;
    }

    /**
     * 单个生成结果
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {

        /** 停止原因 */
        @JsonProperty("finish_reason")
        private String finishReason;

        /** AI回复消息 */
        private ResponseMessage message;
    }

    /**
     * AI回复消息
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseMessage {

        /** 角色，通常为assistant */
        private String role;

        /** 回复内容 */
        private String content;
    }

    /**
     * Token使用统计
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {

        /** 输入token数 */
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        /** 输出token数 */
        @JsonProperty("output_tokens")
        private Integer outputTokens;

        /** 总token数 */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}