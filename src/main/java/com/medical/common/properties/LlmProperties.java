package com.medical.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型配置
 * @author wangda
 * @since 2026/09/30
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm.dash-scope")
public class LlmProperties {

    /** DashScope 基础URL */
    private String baseUrl = "https://dashscope.aliyuncs.com";

    /** API密钥 */
    private String apiKey;

    /** 模型名称，默认qwen-turbo */
    private String model = "qwen-turbo";

    /** 请求超时时间（秒）*/
    private Integer timeoutSeconds = 60;

    /** 采样温度（0-2，越高回复越随机）*/
    private Double temperature = 0.7;

    /** 单次回复最大token数 */
    private Integer maxTokens = 1500;
}