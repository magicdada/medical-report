package com.medical.entity.dos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.medical.common.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

/**
 * 患者扫码对话凭证表
 * @author wangda
 * @since 2026/10/01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "patient_chat")
public class PatientChat extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联报告ID */
    @Column(name = "report_id", nullable = false, length = 32)
    private String reportId;

    /** 访问码(扫码/链接的凭证,唯一)*/
    @Column(name = "access_code", nullable = false, length = 64, unique = true)
    private String accessCode;

    /** 过期时间 */
    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "expire_time", nullable = false)
    private Date expireTime;

    /** 是否已撤销 */
    @Column(nullable = false)
    private Boolean revoked = false;
}