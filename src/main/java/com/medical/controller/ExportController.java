package com.medical.controller;

import com.medical.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;

/**
 * 报告导出接口
 *
 * @author wangda
 * @since 2026/08/12
 */
@Slf4j
@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    /**
     * 导出PDF
     *
     * @param reportId 报告ID
     * @param response 响应
     */
    @GetMapping("/pdf/{reportId}")
    public void exportPdf(@PathVariable String reportId, HttpServletResponse response) {
        exportService.exportPdf(reportId, response);
    }

    /**
     * 导出Word
     *
     * @param reportId 报告ID
     * @param response 响应
     */
    @GetMapping("/word/{reportId}")
    public void exportWord(@PathVariable String reportId, HttpServletResponse response) {
        exportService.exportWord(reportId, response);
    }
}