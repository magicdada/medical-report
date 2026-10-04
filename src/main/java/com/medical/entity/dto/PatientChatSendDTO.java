package com.medical.entity.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 患者对话发送消息DTO
 *
 * @author wangda
 * @since 2026/10/01
 */
@Data
public class PatientChatSendDTO {

    /** 访问码(扫码得到)*/
    @NotBlank(message = "访问码不能为空")
    private String accessCode;

    /** 用户消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String message;
}