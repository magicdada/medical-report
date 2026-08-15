package com.medical.mapper;

import com.medical.entity.dos.Report;
import com.medical.entity.dto.ComparisonStatsDTO;
import com.medical.entity.dto.ReportOverviewDTO;
import com.medical.entity.vos.MonthlyVolumeVO;
import com.medical.entity.vos.ReportVO;
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

    /**
     * 根据患者ID查询报告列表
     * @param patientId
     * @return 报告列表（按创建时间倒序）
     */
    @Query("SELECT new com.medical.entity.vos.ReportVO(" +
            "r.id, r.doctorId, r.patientId, p.patientNo, p.name, " +
            "r.imagePath, r.reportContent, r.aiDraft, r.heatmapPath, " +
            "r.pdfPath, r.status, r.createTime) " +
            "FROM Report r LEFT JOIN Patient p ON r.patientId = p.id " +
            "WHERE r.patientId = :patientId AND r.deleteFlag = false " +
            "ORDER BY r.createTime DESC")
    List<ReportVO> findVOByPatientId(@Param("patientId") String patientId);

    /**
     * 根据医生ID查询报告列表
     * @param doctorId
     * @return 报告列表（按创建时间倒序）
     */
    @Query("SELECT new com.medical.entity.vos.ReportVO(" +
            "r.id, r.doctorId, r.patientId, p.patientNo, p.name, " +
            "r.imagePath, r.reportContent, r.aiDraft, r.heatmapPath, " +
            "r.pdfPath, r.status, r.createTime) " +
            "FROM Report r LEFT JOIN Patient p ON r.patientId = p.id " +
            "WHERE r.doctorId = :doctorId AND r.deleteFlag = false " +
            "ORDER BY r.createTime DESC")
    List<ReportVO> findVOByDoctorId(@Param("doctorId") String doctorId);

    /**
     * 聚合查询医生的报告概览统计数据
     *
     * @param doctorId 医生ID
     * @return 报告概览DTO
     */
    @Query("SELECT new com.medical.entity.dto.ReportOverviewDTO(" +
            "  COUNT(r), " +
            "  SUM(CASE WHEN r.status = 'DRAFT' THEN 1L ELSE 0L END), " +
            "  SUM(CASE WHEN r.status = 'SIGNED' OR r.status = 'CONFIRMED' THEN 1L ELSE 0L END), " +
            "  SUM(CASE WHEN r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL THEN 1L ELSE 0L END), " +
            "  SUM(CASE WHEN r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL AND r.aiDraft = r.reportContent THEN 1L ELSE 0L END)" +
            ") " +
            "FROM Report r WHERE r.doctorId = :doctorId")
    ReportOverviewDTO selectOverviewStats(@Param("doctorId") String doctorId);

    /**
     * 聚合查询医生的报告对比统计指标
     *
     * @param doctorId 医生ID
     * @return 对比统计DTO
     */
    @Query("SELECT new com.medical.entity.dto.ComparisonStatsDTO(" +
            "  SUM(CASE WHEN r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL THEN 1L ELSE 0L END), " +
            "  SUM(CASE WHEN r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL AND r.aiDraft = r.reportContent THEN 1L ELSE 0L END), " +
            "  SUM(CASE WHEN r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL AND r.aiDraft <> r.reportContent " +
            "           AND ABS(LENGTH(r.aiDraft) - LENGTH(r.reportContent)) < LENGTH(r.aiDraft) * 0.2 THEN 1L ELSE 0L END)" +
            ") " +
            "FROM Report r WHERE r.doctorId = :doctorId")
    ComparisonStatsDTO selectComparisonStats(@Param("doctorId") String doctorId);

    /**
     * 按月份分组统计报告数量
     *
     * @param doctorId 医生ID
     * @return 月度统计列表
     */
    @Query("SELECT new com.medical.entity.vos.MonthlyVolumeVO(" +
            "FUNCTION('DATE_FORMAT', r.createTime, '%Y-%m'), COUNT(r)) " +
            "FROM Report r WHERE r.doctorId = ?1 AND r.deleteFlag = false " +
            "GROUP BY FUNCTION('DATE_FORMAT', r.createTime, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', r.createTime, '%Y-%m')")
    List<MonthlyVolumeVO> countByMonth(String doctorId);

    /**
     * 查询报告内容列表（仅取reportContent字段，用于疾病分布统计）
     *
     * @param doctorId 医生ID
     * @return 报告内容列表
     */
    @Query("SELECT r.reportContent FROM Report r WHERE r.doctorId = :doctorId AND r.reportContent IS NOT NULL")
    List<String> findReportContentsByDoctorId(@Param("doctorId") String doctorId);

    /**
     * 查询医生修改过的报告（aiDraft不等于reportContent）
     *
     * @param doctorId 医生ID
     * @return 被修改的报告列表（按创建时间倒序）
     */
    @Query("SELECT r FROM Report r WHERE r.doctorId = :doctorId AND r.aiDraft IS NOT NULL AND r.reportContent IS NOT NULL AND r.aiDraft <> r.reportContent ORDER BY r.createTime DESC")
    List<Report> findModifiedReports(@Param("doctorId") String doctorId);

    /**
     * 统计已签发报告的平均处理时间
     *
     * @param doctorId 医生ID
     * @return 平均处理时间（分钟）
     */
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, create_time, update_time)) FROM report WHERE doctor_id = :doctorId AND status = 'SIGNED' AND create_time IS NOT NULL AND update_time IS NOT NULL", nativeQuery = true)
    Number avgProcessingTime(@Param("doctorId") String doctorId);
}