package com.medical.service;

import com.medical.entity.vos.*;
import java.util.List;

/**
 * 统计业务层
 * @author wangda
 * @since 2026/08/11
 */
public interface StatsService {

    /**
     * 获取总览统计
     *
     * @param doctorId 医生ID
     * @return 总览统计
     */
    OverviewVO getOverview(String doctorId);

    /**
     * 获取月度报告量
     *
     * @param doctorId 医生ID
     * @return 月度数据
     */
    List<MonthlyVolumeVO> getMonthlyVolume(String doctorId);

    /**
     * 获取疾病分布统计
     *
     * @param doctorId 医生ID
     * @return 疾病分布
     */
    List<DiseaseDistributionVO> getDiseaseDistribution(String doctorId);

    /**
     * 获取AI对比统计
     *
     * @param doctorId 医生ID
     * @return 对比统计
     */
    ComparisonStatsVO getComparisonStats(String doctorId);

    /**
     * 获取AI对比记录列表
     *
     * @param doctorId 医生ID
     * @return 对比记录
     */
    List<ComparisonRecordVO> getComparisonRecords(String doctorId);

    /**
     * 获取AI效率统计
     *
     * @param doctorId 医生ID
     * @return 效率统计
     */
    EfficiencyVO getEfficiency(String doctorId);
}
