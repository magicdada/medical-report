package com.medical.entity.dos;

import com.medical.common.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 诊断报告表
 * @author wangda
 * @since 2026/08/08
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "report")
public class Report extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "doctor_id", nullable = false, length = 32)
    private String doctorId;

    @Column(name = "patient_id", nullable = false, length = 32)
    private String patientId;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(name = "report_content", columnDefinition = "TEXT")
    private String reportContent;

    @Column(name = "ai_draft", columnDefinition = "TEXT")
    private String aiDraft;

    @Column(columnDefinition = "TEXT")
    private String impression;

    @Column(length = 20)
    private String gate;

    @Column(name = "report_confidence")
    private Double reportConfidence;

    @Column(name = "findings_keywords", columnDefinition = "TEXT")
    private String findingsKeywords;

    @Column(name = "heatmap_path", columnDefinition = "LONGTEXT")
    private String heatmapPath;

    @Column(name = "pdf_path", length = 255)
    private String pdfPath;

    @Column(length = 20)
    private String status = "DRAFT";
}