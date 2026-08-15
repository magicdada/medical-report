package com.medical.service.impl;

import com.medical.common.enums.DiseaseEnum;
import com.medical.common.util.DateUtil;
import com.medical.entity.dos.Report;
import com.medical.entity.dto.ComparisonStatsDTO;
import com.medical.entity.dto.ReportOverviewDTO;
import com.medical.entity.vos.*;
import com.medical.mapper.PatientMapper;
import com.medical.mapper.ReportMapper;
import com.medical.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计业务层实现
 * @author wangda
 * @since 2026/08/11
 */
@Slf4j
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private PatientMapper patientMapper;

    @Value("${ai.baseline.avg-report-time}")
    private Integer avgReportTimeBaseline;

    @Override
    public OverviewVO getOverview(String doctorId) {
        // 一次性查出聚合数据
        ReportOverviewDTO stats = reportMapper.selectOverviewStats(doctorId);

        OverviewVO vo = new OverviewVO();
        vo.setTotalPatients(patientMapper.count());

        if (stats != null) {
            // 如果某医生一条报告都没有，SUM 可能会返回
            long totalReports = stats.getTotalReports() != null ? stats.getTotalReports() : 0L;
            long draftCount = stats.getDraftCount() != null ? stats.getDraftCount() : 0L;
            long signedCount = stats.getSignedCount() != null ? stats.getSignedCount() : 0L;
            long comparedCount = stats.getComparedCount() != null ? stats.getComparedCount() : 0L;
            long unmodifiedCount = stats.getUnmodifiedCount() != null ? stats.getUnmodifiedCount() : 0L;

            vo.setTotalReports(totalReports);
            vo.setDraftCount(draftCount);
            vo.setSignedCount(signedCount);

            // 计算准确率
            vo.setAccuracy(comparedCount > 0 ? (int) (unmodifiedCount * 100 / comparedCount) : 0);
        }

        return vo;
    }

    @Override
    public List<MonthlyVolumeVO> getMonthlyVolume(String doctorId) {
        List<MonthlyVolumeVO> dbResult = reportMapper.countByMonth(doctorId);
        Map<String, Long> monthCountMap = dbResult.stream()
                .collect(Collectors.toMap(
                        MonthlyVolumeVO::getMonth,
                        MonthlyVolumeVO::getCount,
                        (v1, v2) -> v1 + v2
                ));

        return DateUtil.getRecentMonths(6).stream().map(month -> {
            MonthlyVolumeVO vo = new MonthlyVolumeVO();
            vo.setMonth(month.get("label"));
            vo.setCount(monthCountMap.getOrDefault(month.get("key"), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DiseaseDistributionVO> getDiseaseDistribution(String doctorId) {
        // 只查reportContent字段，不加载完整Report对象
        List<String> contents = reportMapper.findReportContentsByDoctorId(doctorId);

        int total = 0;
        int[] counts = new int[DiseaseEnum.values().length];

        for (String content : contents) {
            if (StringUtils.isBlank(content)){
                continue;
            }
            String lowerContent = content.toLowerCase();
            boolean matched = false;

            for (int i = 1; i < DiseaseEnum.values().length; i++) {
                if (DiseaseEnum.values()[i].matches(lowerContent)) {
                    counts[i]++;
                    matched = true;
                    total++;
                }
            }
            if (!matched && DiseaseEnum.NORMAL.matches(lowerContent)) {
                counts[0]++;
                total++;
            }
        }

        List<DiseaseDistributionVO> result = new ArrayList<>();
        for (int i = 0; i < DiseaseEnum.values().length; i++) {
            DiseaseDistributionVO vo = new DiseaseDistributionVO();
            vo.setName(DiseaseEnum.values()[i].getName());
            vo.setCount(counts[i]);
            vo.setPercent(total > 0 ? counts[i] * 100 / total : 0);
            result.add(vo);
        }
        return result;
    }

    @Override
    public ComparisonStatsVO getComparisonStats(String doctorId) {
        // 一次 SQL 聚合查出所有核心指标
        ComparisonStatsDTO stats = reportMapper.selectComparisonStats(doctorId);

        ComparisonStatsVO vo = new ComparisonStatsVO();
        if (stats == null || stats.getTotal() == null || stats.getTotal() == 0) {
            vo.setTotal(0L);
            vo.setUnmodifiedPercent(0);
            vo.setMinorEditsPercent(0);
            vo.setMajorChangesPercent(0);
            return vo;
        }

        long total = stats.getTotal();
        long unmodifiedCount = stats.getUnmodifiedCount() != null ? stats.getUnmodifiedCount() : 0L;
        long minorEdits = stats.getMinorEditsCount() != null ? stats.getMinorEditsCount() : 0L;

        // 大改数量 = 总修改数 - 微调数
        long modifiedCount = Math.max(0, total - unmodifiedCount);
        long majorChanges = Math.max(0, modifiedCount - minorEdits);

        vo.setTotal(total);
        vo.setUnmodifiedPercent((int) (unmodifiedCount * 100 / total));
        vo.setMinorEditsPercent((int) (minorEdits * 100 / total));
        vo.setMajorChangesPercent((int) (majorChanges * 100 / total));

        return vo;
    }

    @Override
    public List<ComparisonRecordVO> getComparisonRecords(String doctorId) {
        return reportMapper.findModifiedReports(doctorId).stream()
                .map(this::toComparisonRecordVO)
                .collect(Collectors.toList());
    }

    @Override
    public EfficiencyVO getEfficiency(String doctorId) {
        Number avgTime = reportMapper.avgProcessingTime(doctorId);
        int avgTimeWithAi = avgTime != null ? avgTime.intValue() : 0;
        int avgTimeBefore = avgReportTimeBaseline;
        int improvement = avgTimeBefore > 0 && avgTimeWithAi > 0
                ? (avgTimeBefore - avgTimeWithAi) * 100 / avgTimeBefore
                : 0;

        EfficiencyVO vo = new EfficiencyVO();
        vo.setAvgTimeBefore(avgTimeBefore);
        vo.setAvgTimeWithAi(avgTimeWithAi);
        vo.setImprovementPercent(Math.max(improvement, 0));
        return vo;
    }

    private ComparisonRecordVO toComparisonRecordVO(Report report) {
        ComparisonRecordVO vo = new ComparisonRecordVO();
        vo.setId(report.getId());
        vo.setPatientId(report.getPatientId());
        vo.setAiDraft(report.getAiDraft());
        vo.setDoctorFinal(report.getReportContent());
        vo.setCreateTime(report.getCreateTime());
        return vo;
    }
}