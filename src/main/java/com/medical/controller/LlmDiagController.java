package com.medical.controller;

import com.medical.common.ResultMessage;
import com.medical.common.util.ResultUtil;
import com.medical.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;

/**
 * 大模型联调诊断接口
 * @author wangda
 * @since 2026/09/30
 */
@Slf4j
@RestController
@RequestMapping("/api/llm-diag")
public class LlmDiagController {

    @Autowired
    private LlmService llmService;

    /**
     * 大模型连通性检测
     * @param message 用户消息
     * @return AI回复
     */
    @GetMapping("/ping")
    public ResultMessage<String> ping(@RequestParam(defaultValue = "你好，请一句话介绍自己") String message) {
        String reply = llmService.chat(
                "你是一个友善的助手，用一两句话简洁回答。",
                new ArrayList<>(),
                message
        );
        return ResultUtil.data(reply);
    }
}