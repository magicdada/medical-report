package com.medical.entity.vos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

/**
 * 报告列表VO
 *
 * @author wangda
 * @since 2026/08/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportVO {

    /** 报告ID */
    private String id;

    /** 医生ID */
    private String doctorId;

    /** 患者ID */
    private String patientId;

    /** 患者编号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 影像路径 */
    private String imagePath;

    /** 报告内容 */
    private String reportContent;

    /** AI原稿 */
    private String aiDraft;

    /** 热力图路径 */
    private String heatmapPath;

    /** PDF路径 */
    private String pdfPath;

    /** 报告状态 */
    private String status;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}