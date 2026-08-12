package com.medical.service;

import javax.servlet.http.HttpServletResponse;

/**
 * 报告导出业务层
 *
 * @author wangda
 * @since 2026/08/12
 */
public interface ExportService {

    /**
     * 导出PDF
     *
     * @param reportId 报告ID
     * @param response 响应
     */
    void exportPdf(String reportId, HttpServletResponse response);

    /**
     * 导出Word
     *
     * @param reportId 报告ID
     * @param response 响应
     */
    void exportWord(String reportId, HttpServletResponse response);
}