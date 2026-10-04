package com.medical.entity.vos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 患者端报告展示VO
 *
 * @author wangda
 * @since 2026/10/01
 */
@Data
public class PatientReportVO {

    /** 报告ID */
    private String reportId;

    /** 患者姓名 */
    private String patientName;

    /** 诊断结论 */
    private String impression;

    /** 检查日期 */
    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportDate;

    /** 温馨提示( */
    private String disclaimer;
}