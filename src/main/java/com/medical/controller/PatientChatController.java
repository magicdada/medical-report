package com.medical.controller;

import com.medical.common.ResultMessage;
import com.medical.common.util.ResultUtil;
import com.medical.entity.dto.PatientChatSendDTO;
import com.medical.entity.vos.ChatMessageVO;
import com.medical.entity.vos.PatientReportVO;
import com.medical.service.PatientChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 患者扫码对话接口
 *
 * @author wangda
 * @since 2026/10/01
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/patient-chat")
public class PatientChatController {

    @Autowired
    private PatientChatService patientChatService;

    /**
     * 根据访问码获取报告
     *
     * @param accessCode 访问码
     * @return 患者端报告信息
     */
    @GetMapping("/report/{accessCode}")
    public ResultMessage<PatientReportVO> getReport(
            @NotBlank(message = "访问码不能为空") @PathVariable String accessCode) {
        return ResultUtil.data(patientChatService.getReportByAccessCode(accessCode));
    }

    /**
     * 患者发送消息,获取AI回复
     *
     * @param dto 请求参数
     * @return AI回复消息
     */
    @PostMapping("/send")
    public ResultMessage<ChatMessageVO> send(@Valid @RequestBody PatientChatSendDTO dto) {
        ChatMessageVO reply = patientChatService.sendMessage(dto.getAccessCode(), dto.getMessage());
        return ResultUtil.data(reply);
    }

    /**
     * 获取患者对话历史
     *
     * @param accessCode 访问码
     * @return 对话历史列表
     */
    @GetMapping("/history/{accessCode}")
    public ResultMessage<List<ChatMessageVO>> history(
            @NotBlank(message = "访问码不能为空") @PathVariable String accessCode) {
        return ResultUtil.data(patientChatService.getHistory(accessCode));
    }
}