package com.medical.entity.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 医生对话发送消息DTO
 * @author wangda
 * @since 2026/09/30
 */
@Data
public class DoctorChatSendDTO {

    /** 关联报告ID */
    @NotBlank(message = "报告ID不能为空")
    private String reportId;

    /** 用户消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String message;
}