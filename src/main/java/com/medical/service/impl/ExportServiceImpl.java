package com.medical.service.impl;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.medical.common.ResultCode;
import com.medical.common.ServiceException;
import com.medical.common.util.DateUtil;
import com.medical.entity.dos.Doctor;
import com.medical.entity.dos.Patient;
import com.medical.entity.dos.Report;
import com.medical.mapper.DoctorMapper;
import com.medical.mapper.PatientMapper;
import com.medical.mapper.ReportMapper;
import com.medical.service.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
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

    @Autowired
    private DoctorMapper doctorMapper;

    @Override
    public void exportPdf(String reportId, String doctorId, HttpServletResponse response) {
        Report report = getReportOrThrow(reportId);
        checkOwnership(report, doctorId);
        Patient patient = getPatientOrThrow(report.getPatientId());
        Doctor doctor = getDoctorOrThrow(doctorId);

        ServletOutputStream out = null;
        try {
            response.setContentType("application/pdf");
            String fileName = patient.getName() + "_" + DateUtil.toString(report.getCreateTime(), "yyyy-MM-dd");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".pdf");
            out = response.getOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(40, 50, 40, 50);

            // 颜色定义
            Color headerBg = new DeviceRgb(20, 25, 80);
            Color headerText = ColorConstants.WHITE;
            Color borderColor = new DeviceRgb(180, 185, 200);

            // 标题
            document.add(new Paragraph("Radiology Report")
                    .setFontSize(22).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            // 患者信息
            addSectionHeader(document, "Patient Information", headerBg, headerText);
            Table patientTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setBorder(new SolidBorder(borderColor, 0.5f));
            addFormRow(patientTable, "Name:", patient.getName(), borderColor);
            addFormRow(patientTable, "Age:", patient.getAge() != null ? patient.getAge().toString() : "", borderColor);
            addFormRow(patientTable, "Gender:", patient.getGender(), borderColor);
            addFormRow(patientTable, "Medical Record Number:", report.getId(), borderColor);
            document.add(patientTable);
            document.add(new Paragraph("").setMarginBottom(5));

            // Technique
            addSectionHeader(document, "Technique", headerBg, headerText);
            addSectionContent(document, "PA and Lateral Chest Radiograph", borderColor);

            // Findings
            addSectionHeader(document, "Findings", headerBg, headerText);
            addSectionContent(document,
                    report.getReportContent() != null ? report.getReportContent() : "No findings recorded.",
                    borderColor);

            // Impressions
            addSectionHeader(document, "Impressions", headerBg, headerText);
            addSectionContent(document,
                    report.getImpression() != null ? report.getImpression() : "No impression recorded.",
                    borderColor);

            // Recommendations
            addSectionHeader(document, "Recommendations", headerBg, headerText);
            addSectionContent(document, " ", borderColor);

            document.add(new Paragraph("").setMarginBottom(5));

            // 签名区
            Table signTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setBorder(new SolidBorder(borderColor, 0.5f));
            addFormRow(signTable, "Radiologist's Name:", doctor.getRealName(), borderColor);
            addFormRow(signTable, "Date:", DateUtil.toString(report.getCreateTime()), borderColor);
            addFormRow(signTable, "Signature:", "",borderColor);
            document.add(signTable);

            // 底部声明
            document.add(new Paragraph("")
                    .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 1)).setMarginTop(15).setMarginBottom(8));
            document.add(new Paragraph("This report was generated with AI assistance. Please review and confirm before clinical use.")
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
    public void exportWord(String reportId, String doctorId, HttpServletResponse response) {
        Report report = getReportOrThrow(reportId);
        checkOwnership(report, doctorId);
        Patient patient = getPatientOrThrow(report.getPatientId());
        Doctor doctor = getDoctorOrThrow(doctorId);

        ServletOutputStream out = null;
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document;charset=utf-8");
            String fileName = patient.getName() + "_" + DateUtil.toString(report.getCreateTime(), "yyyy-MM-dd");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".docx");
            out = response.getOutputStream();

            XWPFDocument document = new XWPFDocument();

            // 标题
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            title.setSpacingAfter(400);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Radiology Report");
            titleRun.setBold(true);
            titleRun.setFontSize(22);

            // 患者信息
            addWordSectionHeader(document, "Patient Information");
            XWPFTable patientTable = document.createTable(4, 2);
            patientTable.setWidth("100%");
            setTableCell(patientTable, 0, 0, "Name:");
            setTableCell(patientTable, 0, 1, patient.getName());
            setTableCell(patientTable, 1, 0, "Age:");
            setTableCell(patientTable, 1, 1, patient.getAge() != null ? patient.getAge().toString() : "");
            setTableCell(patientTable, 2, 0, "Gender:");
            setTableCell(patientTable, 2, 1, patient.getGender());
            setTableCell(patientTable, 3, 0, "Medical Record Number:");
            setTableCell(patientTable, 3, 1, report.getId());

            document.createParagraph();

            // Technique
            addWordSectionHeader(document, "Technique");
            addWordSectionContent(document, "PA and Lateral Chest Radiograph");

            // Findings
            addWordSectionHeader(document, "Findings");
            addWordSectionContent(document,
                    report.getReportContent() != null ? report.getReportContent() : "No findings recorded.");

            // Impressions
            addWordSectionHeader(document, "Impressions");
            addWordSectionContent(document,
                    report.getImpression() != null ? report.getImpression() : "No impression recorded.");

            // Recommendations
            addWordSectionHeader(document, "Recommendations");
            addWordSectionContent(document, " ");

            document.createParagraph();

            // 签名区
            XWPFTable signTable = document.createTable(3, 2);
            signTable.setWidth("100%");
            setTableCell(signTable, 0, 0, "Radiologist's Name:");
            setTableCell(signTable, 0, 1, doctor.getRealName());
            setTableCell(signTable, 1, 0, "Date:");
            setTableCell(signTable, 1, 1, DateUtil.toString(report.getCreateTime()));
            setTableCell(signTable, 2, 0, "Signature:");
            setTableCell(signTable, 2, 1, "");

            document.createParagraph();

            // 底部声明
            XWPFParagraph disclaimer = document.createParagraph();
            disclaimer.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun disclaimerRun = disclaimer.createRun();
            disclaimerRun.setText("This report was generated with AI assistance. Please review and confirm before clinical use.");
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

    private Doctor getDoctorOrThrow(String doctorId) {
        Doctor doctor = doctorMapper.findById(doctorId).orElse(null);
        if (doctor == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        return doctor;
    }

    private void addSectionHeader(Document document, String title, Color bgColor, Color textColor) {
        Table table = new Table(1).useAllAvailableWidth();
        Cell cell = new Cell().add(new Paragraph(title).setFontSize(11).setBold().setFontColor(textColor))
                .setBackgroundColor(bgColor)
                .setPadding(6)
                .setBorder(Border.NO_BORDER);
        table.addCell(cell);
        document.add(table);
    }

    private void addSectionContent(Document document, String content, Color borderColor) {
        Table table = new Table(1).useAllAvailableWidth()
                .setBorder(new SolidBorder(borderColor, 0.5f));
        Cell cell = new Cell().add(new Paragraph(content).setFontSize(10))
                .setPadding(8)
                .setMinHeight(25)
                .setBorder(new SolidBorder(borderColor, 0.5f));
        table.addCell(cell);
        document.add(table);
        document.add(new Paragraph("").setMarginBottom(4));
    }

    private void addFormRow(Table table, String label, String value, Color borderColor) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFontSize(10).setBold())
                .setPadding(6)
                .setBorder(new SolidBorder(borderColor, 0.5f));
        Cell valueCell = new Cell().add(new Paragraph(value != null ? value : "").setFontSize(10))
                .setPadding(6)
                .setBorder(new SolidBorder(borderColor, 0.5f));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void setTableCell(XWPFTable table, int row, int col, String text) {
        table.getRow(row).getCell(col).setText(text != null ? text : "");
    }

    private void addWordSectionHeader(XWPFDocument document, String title) {
        XWPFParagraph p = document.createParagraph();
        p.setSpacingBefore(100);
        p.setSpacingAfter(0);

        CTShd shd = p.getCTP().addNewPPr().addNewShd();
        shd.setVal(STShd.CLEAR);
        shd.setFill("141950");

        XWPFRun run = p.createRun();
        run.setText(title);
        run.setBold(true);
        run.setFontSize(11);
        run.setColor("FFFFFF");
    }

    private void addWordSectionContent(XWPFDocument document, String content) {
        XWPFTable table = document.createTable(1, 1);
        table.setWidth("100%");
        XWPFTableCell cell = table.getRow(0).getCell(0);
        XWPFParagraph p = cell.getParagraphs().get(0);
        XWPFRun run = p.createRun();
        run.setText(content);
        run.setFontSize(10);
        document.createParagraph();
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