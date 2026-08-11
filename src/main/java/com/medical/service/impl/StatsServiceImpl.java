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
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public OverviewVO getOverview(String doctorId) {
        List<Report> reports = reportMapper.findByDoctorIdOrderByCreateTimeDesc(doctorId);

        long draftCount = reports.stream()
                .filter(r -> ReportStatusEnum.DRAFT.name().equals(r.getStatus()))
                .count();
        long signedCount = reports.stream()
                .filter(r -> ReportStatusEnum.SIGNED.name().equals(r.getStatus())
                        || ReportStatusEnum.CONFIRMED.name().equals(r.getStatus()))
                .count();
        long comparedCount = reports.stream().filter(this::hasAiDraft).count();
        long unmodifiedCount = reports.stream().filter(this::hasAiDraft).filter(this::isUnmodified).count();

        OverviewVO vo = new OverviewVO();
        vo.setTotalPatients(patientMapper.count());
        vo.setTotalReports((long) reports.size());
        vo.setDraftCount(draftCount);
        vo.setSignedCount(signedCount);
        vo.setAccuracy(comparedCount > 0 ? (int) (unmodifiedCount * 100 / comparedCount) : 0);
        return vo;
    }

    @Override
    public List<MonthlyVolumeVO> getMonthlyVolume(String doctorId) {
        List<Report> reports = reportMapper.findByDoctorIdOrderByCreateTimeDesc(doctorId);

        return DateUtil.getRecentMonths(6).stream().map(month -> {
            MonthlyVolumeVO vo = new MonthlyVolumeVO();
            vo.setMonth(month.get("label"));
            vo.setCount(countReportsByMonth(reports, month.get("key")));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DiseaseDistributionVO> getDiseaseDistribution(String doctorId) {
        List<Report> reports = reportMapper.findByDoctorIdOrderByCreateTimeDesc(doctorId);

        int total = 0;
        int[] counts = new int[DiseaseEnum.values().length];

        for (Report report : reports) {
            if (StringUtils.isNotBlank(report.getReportContent())) {
                continue;
            }
            String content = report.getReportContent().toLowerCase();
            boolean matched = false;

            // 先匹配疾病类（跳过Normal）
            for (int i = 1; i < DiseaseEnum.values().length; i++) {
                if (DiseaseEnum.values()[i].matches(content)) {
                    counts[i]++;
                    matched = true;
                    total++;
                }
            }
            // 没有匹配到疾病，检查是否正常
            if (!matched && DiseaseEnum.NORMAL.matches(content)) {
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
        List<Report> compared = reportMapper.findByDoctorIdOrderByCreateTimeDesc(doctorId)
                .stream().filter(this::hasAiDraft).collect(Collectors.toList());

        long total = compared.size();
        long unmodified = compared.stream().filter(this::isUnmodified).count();
        long minorEdits = compared.stream()
                .filter(r -> !isUnmodified(r))
                .filter(this::isMinorEdit)
                .count();
        long majorChanges = total - unmodified - minorEdits;

        ComparisonStatsVO vo = new ComparisonStatsVO();
        vo.setTotal(total);
        vo.setUnmodifiedPercent(total > 0 ? (int) (unmodified * 100 / total) : 0);
        vo.setMinorEditsPercent(total > 0 ? (int) (minorEdits * 100 / total) : 0);
        vo.setMajorChangesPercent(total > 0 ? (int) (majorChanges * 100 / total) : 0);
        return vo;
    }

    @Override
    public List<ComparisonRecordVO> getComparisonRecords(String doctorId) {
        return reportMapper.findByDoctorIdOrderByCreateTimeDesc(doctorId)
                .stream()
                .filter(this::hasAiDraft)
                .filter(r -> !isUnmodified(r))
                .map(this::toComparisonRecordVO)
                .collect(Collectors.toList());
    }

    /**
     * 判断报告是否有AI原稿
     */
    private boolean hasAiDraft(Report report) {
        return StringUtils.isNotBlank(report.getAiDraft()) && StringUtils.isNotBlank(report.getReportContent());
    }

    /**
     * 判断报告是否未被医生修改
     */
    private boolean isUnmodified(Report report) {
        return report.getAiDraft().equals(report.getReportContent());
    }

    /**
     * 判断是否为小幅修改（修改字数小于原文20%）
     */
    private boolean isMinorEdit(Report report) {
        int diff = Math.abs(report.getAiDraft().length() - report.getReportContent().length());
        return diff < report.getAiDraft().length() * 0.2;
    }

    /**
     * 统计某个月份的报告数量
     */
    private long countReportsByMonth(List<Report> reports, String monthKey) {
        return reports.stream()
                .filter(r -> r.getCreateTime() != null)
                .filter(r -> DateUtil.getMonthKey(r.getCreateTime()).equals(monthKey))
                .count();
    }

    /**
     * Report转ComparisonRecordVO
     */
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
