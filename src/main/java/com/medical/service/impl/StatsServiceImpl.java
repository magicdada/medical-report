package com.medical.service.impl;

import com.medical.common.enums.DiseaseEnum;
import com.medical.common.enums.ReportStatusEnum;
import com.medical.common.util.DateUtil;
import com.medical.entity.dos.Report;
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
 *
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
        long comparedCount = reportMapper.countCompared(doctorId);
        long unmodifiedCount = reportMapper.countUnmodified(doctorId);

        OverviewVO vo = new OverviewVO();
        vo.setTotalPatients(patientMapper.count());
        vo.setTotalReports(reportMapper.countByDoctorId(doctorId));
        vo.setDraftCount(reportMapper.countByDoctorIdAndStatus(doctorId, ReportStatusEnum.DRAFT.name()));
        vo.setSignedCount(
                reportMapper.countByDoctorIdAndStatus(doctorId, ReportStatusEnum.SIGNED.name())
                        + reportMapper.countByDoctorIdAndStatus(doctorId, ReportStatusEnum.CONFIRMED.name())
        );
        vo.setAccuracy(comparedCount > 0 ? (int) (unmodifiedCount * 100 / comparedCount) : 0);
        return vo;
    }

    @Override
    public List<MonthlyVolumeVO> getMonthlyVolume(String doctorId) {
        List<MonthlyVolumeVO> dbResult = reportMapper.countByMonth(doctorId);
        Map<String, Long> monthCountMap = dbResult.stream()
                .collect(Collectors.toMap(
                        MonthlyVolumeVO::getMonth,
                        MonthlyVolumeVO::getCount,
                        (v1, v2) -> v1 + v2 // 遇到重复 key 时相加合并，增强容错性
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
        long comparedCount = reportMapper.countCompared(doctorId);
        long unmodifiedCount = reportMapper.countUnmodified(doctorId);
        long modifiedCount = comparedCount - unmodifiedCount;

        // 查询被修改的报告来区分minor和major
        List<Report> modifiedReports = reportMapper.findModifiedReports(doctorId);
        long minorEdits = modifiedReports.stream().filter(this::isMinorEdit).count();
        long majorChanges = modifiedCount - minorEdits;

        ComparisonStatsVO vo = new ComparisonStatsVO();
        vo.setTotal(comparedCount);
        vo.setUnmodifiedPercent(comparedCount > 0 ? (int) (unmodifiedCount * 100 / comparedCount) : 0);
        vo.setMinorEditsPercent(comparedCount > 0 ? (int) (minorEdits * 100 / comparedCount) : 0);
        vo.setMajorChangesPercent(comparedCount > 0 ? (int) (majorChanges * 100 / comparedCount) : 0);
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

    private boolean isMinorEdit(Report report) {
        int diff = Math.abs(report.getAiDraft().length() - report.getReportContent().length());
        return diff < report.getAiDraft().length() * 0.2;
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