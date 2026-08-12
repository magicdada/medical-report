package com.medical.controller;

import com.medical.common.ResultMessage;
import com.medical.common.util.ResultUtil;
import com.medical.common.security.AuthUser;
import com.medical.common.security.UserContext;
import com.medical.entity.vos.*;
import com.medical.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 统计接口
 * @author wangda
 * @since 2026/08/11
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 获取总览统计
     *
     * @return 总览统计
     */
    @GetMapping("/overview")
    public ResultMessage<OverviewVO> overview() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(statsService.getOverview(authUser.getId()));
    }

    /**
     * 获取月度报告量
     *
     * @return 月度数据
     */
    @GetMapping("/monthly")
    public ResultMessage<List<MonthlyVolumeVO>> monthly() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(statsService.getMonthlyVolume(authUser.getId()));
    }

    /**
     * 获取疾病分布
     *
     * @return 疾病分布
     */
    @GetMapping("/disease")
    public ResultMessage<List<DiseaseDistributionVO>> disease() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(statsService.getDiseaseDistribution(authUser.getId()));
    }

    /**
     * 获取AI对比统计
     *
     * @return 对比统计
     */
    @GetMapping("/comparison")
    public ResultMessage<ComparisonStatsVO> comparison() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(statsService.getComparisonStats(authUser.getId()));
    }

    /**
     * 获取AI对比记录列表
     *
     * @return 对比记录
     */
    @GetMapping("/comparison/records")
    public ResultMessage<List<ComparisonRecordVO>> comparisonRecords() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(statsService.getComparisonRecords(authUser.getId()));
    }

    /**
     * 获取AI效率统计
     *
     * @return 效率统计
     */
    @GetMapping("/efficiency")
    public ResultMessage<EfficiencyVO> efficiency() {
        AuthUser authUser = UserContext.getCurrentUser();
        return ResultUtil.data(statsService.getEfficiency(authUser.getId()));
    }
}
