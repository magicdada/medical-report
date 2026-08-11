package com.medical.entity.vos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * AI对比记录VO
 *
 * @author wangda
 * @since 2026/08/11
 */
@Data
public class ComparisonRecordVO {

    /** 报告ID */
    private String id;

    /** 患者ID */
    private String patientId;

    /** AI原始报告 */
    private String aiDraft;

    /** 医生修改后报告 */
    private String doctorFinal;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
