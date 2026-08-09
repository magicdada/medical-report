package com.medical.mapper;

import com.medical.entity.dos.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 诊断报告数据处理层
 * @author wangda
 * @since 2026/08/08
 */
public interface ReportMapper extends JpaRepository<Report, String> {

    List<Report> findByPatientIdOrderByCreateTimeDesc(String patientId);

    List<Report> findByDoctorIdOrderByCreateTimeDesc(String doctorId);
}