package com.medical.controller;

import com.medical.common.ResultMessage;
import com.medical.common.security.UserContext;
import com.medical.common.util.ResultUtil;
import com.medical.entity.dto.DoctorChatSendDTO;
import com.medical.entity.vos.ChatMessageVO;
import com.medical.service.DoctorChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 医生对话接口
 * @author wangda
 * @since 2026/09/30
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/doctor-chat")
public class DoctorChatController {

    @Autowired
    private DoctorChatService doctorChatService;

    /**
     * 医生发送消息，获取AI回复
     * @param dto 请求参数
     * @return AI回复消息
     */
    @PostMapping("/send")
    public ResultMessage<ChatMessageVO> send(@Valid @RequestBody DoctorChatSendDTO dto) {
        String doctorId = UserContext.getCurrentUser().getId();
        ChatMessageVO reply = doctorChatService.sendMessage(dto.getReportId(), dto.getMessage(), doctorId);
        return ResultUtil.data(reply);
    }

    /**
     * 获取医生对话历史
     * @param reportId 报告ID
     * @return 对话历史列表
     */
    @GetMapping("/history/{reportId}")
    public ResultMessage<List<ChatMessageVO>> history(@NotBlank(message = "报告ID不能为空") @PathVariable String reportId) {
        String doctorId = UserContext.getCurrentUser().getId();
        return ResultUtil.data(doctorChatService.getHistory(reportId, doctorId));
    }

    /**
     * 清空医生对话历史
     * @param reportId 报告ID
     * @return 操作结果
     */
    @DeleteMapping("/history/{reportId}")
    public ResultMessage<Object> clear(@NotBlank(message = "报告ID不能为空") @PathVariable String reportId) {
        String doctorId = UserContext.getCurrentUser().getId();
        doctorChatService.clearHistory(reportId, doctorId);
        return ResultUtil.success();
    }
}