package com.medical.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 患者扫码对话配置
 * @author wangda
 * @since 2026/10/01
 */
@Data
@Component
@ConfigurationProperties(prefix = "patient-chat")
public class PatientChatProperties {

    /** 患者端前端访问域名 */
    private String frontendBaseUrl;

    /** 访问码有效期(天)*/
    private Integer expireDays = 30;

    /** QR 码图片尺寸(像素)*/
    private Integer qrcodeSize = 180;
}