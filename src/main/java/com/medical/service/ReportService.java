package com.medical.service;

import com.medical.entity.dos.Report;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 诊断报告业务层
 * @author wangda
 * @since 2026/08/08
 */
public interface ReportService {

    /**
     * 生成诊断报告
     *
     * @param doctorId  医生ID
     * @param patientId 患者ID
     * @param imageFile 影像文件
     * @return 报告信息
     */
    Report generateReport(String doctorId, String patientId, MultipartFile imageFile);

    /**
     * 根据ID获取报告
     *
     * @param id 报告ID
     * @return 报告信息
     */
    Report getById(String id);

    /**
     * 根据患者ID获取报告列表
     *
     * @param patientId 患者ID
     * @return 报告列表
     */
    List<Report> getByPatientId(String patientId);

    /**
     * 根据医生ID获取报告列表
     *
     * @param doctorId 医生ID
     * @return 报告列表
     */
    List<Report> getByDoctorId(String doctorId);

    /**
     * 更新报告状态
     *
     * @param id       报告ID
     * @param status   状态
     * @param doctorId 当前医生ID
     * @return 报告信息
     */
    Report updateStatus(String id, String status, String doctorId);
}