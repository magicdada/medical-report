package com.medical.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.medical.common.ResultCode;
import com.medical.common.ServiceException;
import com.medical.common.util.DateUtil;
import com.medical.entity.dos.Patient;
import com.medical.entity.dos.Report;
import com.medical.mapper.PatientMapper;
import com.medical.mapper.ReportMapper;
import com.medical.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

/**
 * 报告导出业务层实现
 *
 * @author wangda
 * @since 2026/08/12
 */
@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public void exportPdf(String reportId, String doctorId,HttpServletResponse response) {
        Report report = getReportOrThrow(reportId);
        checkOwnership(report, doctorId);
        Patient patient = getPatientOrThrow(report.getPatientId());

        ServletOutputStream out = null;
        try {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("report_" + reportId, "UTF-8") + ".pdf");
            out = response.getOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(50, 50, 50, 50);

            // 标题
            document.add(new Paragraph("Medical Imaging Diagnostic Report")
                    .setFontSize(18).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));

            document.add(new Paragraph("AI-Assisted Chest X-Ray Analysis")
                    .setFontSize(10).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            // 患者信息表格
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                    .useAllAvailableWidth().setMarginBottom(15);
            addInfoCell(infoTable, "Patient No.", patient.getPatientNo());
            addInfoCell(infoTable, "Name", patient.getName());
            addInfoCell(infoTable, "Gender", patient.getGender());
            addInfoCell(infoTable, "Age", patient.getAge() != null ? patient.getAge().toString() : "");
            addInfoCell(infoTable, "Report Date", DateUtil.toString(report.getCreateTime()));
            addInfoCell(infoTable, "Status", report.getStatus());
            document.add(infoTable);

            // 分割线
            document.add(new Paragraph("")
                    .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 1)).setMarginBottom(15));

            // Findings
            document.add(new Paragraph("FINDINGS")
                    .setFontSize(11).setBold().setFontColor(ColorConstants.DARK_GRAY).setMarginBottom(5));
            document.add(new Paragraph(report.getReportContent() != null ? report.getReportContent() : "No content")
                    .setFontSize(10).setMarginBottom(15));

            // Impression
            document.add(new Paragraph("IMPRESSION")
                    .setFontSize(11).setBold().setFontColor(ColorConstants.DARK_GRAY).setMarginBottom(5));
            document.add(new Paragraph("No acute cardiopulmonary abnormality.")
                    .setFontSize(10).setMarginBottom(20));

            // 底部声明
            document.add(new Paragraph("")
                    .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 1)).setMarginBottom(10));
            document.add(new Paragraph("This report was generated with AI assistance (R2GenGPT). Please review and confirm before clinical use.")
                    .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));

            document.close();
            log.info("PDF导出成功，报告ID：{}", reportId);

        } catch (Exception e) {
            log.error("PDF导出失败", e);
            throw new ServiceException(ResultCode.REPORT_GENERATE_ERROR, "PDF导出失败");
        } finally {
            closeStream(out);
        }
    }

    @Override
    public void exportWord(String reportId,String doctorId, HttpServletResponse response) {
        Report report = getReportOrThrow(reportId);
        checkOwnership(report, doctorId);
        Patient patient = getPatientOrThrow(report.getPatientId());

        ServletOutputStream out = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document;charset=utf-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("report_" + reportId, "UTF-8") + ".docx");
            out = response.getOutputStream();

            XWPFDocument document = new XWPFDocument();

            // 标题
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Medical Imaging Diagnostic Report");
            titleRun.setBold(true);
            titleRun.setFontSize(18);

            // 副标题
            XWPFParagraph subtitle = document.createParagraph();
            subtitle.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun subtitleRun = subtitle.createRun();
            subtitleRun.setText("AI-Assisted Chest X-Ray Analysis");
            subtitleRun.setFontSize(10);
            subtitleRun.setColor("888888");

            document.createParagraph();

            // 患者信息表格
            XWPFTable infoTable = document.createTable(3, 4);
            infoTable.setWidth("100%");
            setTableCell(infoTable, 0, 0, "Patient No.");
            setTableCell(infoTable, 0, 1, patient.getPatientNo());
            setTableCell(infoTable, 0, 2, "Name");
            setTableCell(infoTable, 0, 3, patient.getName());
            setTableCell(infoTable, 1, 0, "Gender");
            setTableCell(infoTable, 1, 1, patient.getGender());
            setTableCell(infoTable, 1, 2, "Age");
            setTableCell(infoTable, 1, 3, patient.getAge() != null ? patient.getAge().toString() : "");
            setTableCell(infoTable, 2, 0, "Report Date");
            setTableCell(infoTable, 2, 1, DateUtil.toString(report.getCreateTime()));
            setTableCell(infoTable, 2, 2, "Status");
            setTableCell(infoTable, 2, 3, report.getStatus());

            document.createParagraph();

            // Findings
            addWordSection(document, "FINDINGS",
                    report.getReportContent() != null ? report.getReportContent() : "No content");

            document.createParagraph();

            // Impression
            addWordSection(document, "IMPRESSION",
                    "No acute cardiopulmonary abnormality.");

            document.createParagraph();

            // 声明
            XWPFParagraph disclaimer = document.createParagraph();
            disclaimer.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun disclaimerRun = disclaimer.createRun();
            disclaimerRun.setText("This report was generated with AI assistance (R2GenGPT). Please review and confirm before clinical use.");
            disclaimerRun.setFontSize(8);
            disclaimerRun.setColor("888888");

            document.write(out);
            document.close();
            log.info("Word导出成功，报告ID：{}", reportId);

        } catch (Exception e) {
            log.error("Word导出失败", e);
            throw new ServiceException(ResultCode.REPORT_GENERATE_ERROR, "Word导出失败");
        } finally {
            closeStream(out);
        }
    }

    private Report getReportOrThrow(String reportId) {
        Report report = reportMapper.findById(reportId).orElse(null);
        if (report == null) {
            throw new ServiceException(ResultCode.REPORT_NOT_EXIST);
        }
        return report;
    }

    private Patient getPatientOrThrow(String patientId) {
        Patient patient = patientMapper.findById(patientId).orElse(null);
        if (patient == null) {
            throw new ServiceException(ResultCode.PATIENT_NOT_EXIST);
        }
        return patient;
    }

    private void addInfoCell(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setFontSize(9).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "").setFontSize(9)));
    }

    private void setTableCell(XWPFTable table, int row, int col, String text) {
        table.getRow(row).getCell(col).setText(text != null ? text : "");
    }

    private void addWordSection(XWPFDocument document, String title, String content) {
        XWPFParagraph titleParagraph = document.createParagraph();
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText(title);
        titleRun.setBold(true);
        titleRun.setFontSize(11);

        XWPFParagraph contentParagraph = document.createParagraph();
        XWPFRun contentRun = contentParagraph.createRun();
        contentRun.setText(content);
        contentRun.setFontSize(10);
    }

    /**
     * 校验报告归属权
     */
    private void checkOwnership(Report report, String doctorId) {
        if (!report.getDoctorId().equals(doctorId)) {
            throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
    }

    private void closeStream(ServletOutputStream out) {
        if (out != null) {
            try {
                out.flush();
                out.close();
            } catch (Exception e) {
                log.error("输出流关闭失败", e);
            }
        }
    }
}