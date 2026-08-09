package com.medical.entity.dos;

import com.medical.common.BaseEntity;
import javax.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者表
 * @author wangda
 * @since 2026/08/08
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "patient")
public class Patient extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "patient_no", nullable = false, length = 50)
    private String patientNo;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String gender;

    private Integer age;

    @Column(name = "medical_history", length = 500)
    private String medicalHistory;
}