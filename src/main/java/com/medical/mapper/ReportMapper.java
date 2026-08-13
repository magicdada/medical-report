package com.medical.mapper;

import com.medical.entity.dos.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * 诊断报告数据处理层
 *
 * @author wangda
 * @since 2026/08/08
 */
public interface ReportMapper extends JpaRepository<Report, String> {

    List<Report> findByPatientIdOrderByCreateTimeDesc(String patientId);

    List<Report> findByDoctorIdOrderByCreateTimeDesc(String doctorId);

    /**
     * 统计医生的报告总数
     */
    long countByDoctorId(String doctorId);

    /**
     * 按状态统计医生的报告数
     */
    long countByDoctorIdAndStatus(String doctorId, String status);

    /**
     * 统计未被修改的报告数（aiDraft等于reportContent）
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.doctorId = :doctorId AND r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL AND r.aiDraft = r.reportContent")
    long countUnmodified(@Param("doctorId") String doctorId);

    /**
     * 统计有AI原稿的报告数
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.doctorId = :doctorId AND r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL")
    long countCompared(@Param("doctorId") String doctorId);

    /**
     * 按月份统计报告数
     */
    @Query(value = "SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) FROM report WHERE doctor_id = :doctorId GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY month", nativeQuery = true)
    List<Object[]> countByMonth(@Param("doctorId") String doctorId);

    /**
     * 查询报告内容（仅用于疾病分布统计，只取reportContent字段）
     */
    @Query("SELECT r.reportContent FROM Report r WHERE r.doctorId = :doctorId AND r.reportContent IS NOT NULL")
    List<String> findReportContentsByDoctorId(@Param("doctorId") String doctorId);

    /**
     * 查询有差异的对比记录（aiDraft不等于reportContent）
     */
    @Query("SELECT r FROM Report r WHERE r.doctorId = :doctorId AND r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL AND r.aiDraft <> r.reportContent ORDER BY r.createTime DESC")
    List<Report> findModifiedReports(@Param("doctorId") String doctorId);

    /**
     * 统计已签发报告的平均处理时间（分钟）
     */
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, create_time, update_time)) FROM report WHERE doctor_id = :doctorId AND status = 'SIGNED' AND create_time IS NOT NULL AND update_time IS NOT NULL", nativeQuery = true)
    Double avgProcessingTime(@Param("doctorId") String doctorId);
}