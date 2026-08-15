package com.medical.controller;

import com.medical.common.ResultMessage;
import com.medical.common.util.ResultUtil;
import com.medical.common.security.AuthUser;
import com.medical.common.security.UserContext;
import com.medical.entity.dos.Report;
import com.medical.entity.vos.ReportVO;
import com.medical.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 诊断报告接口
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 上传影像并生成报告
     *
     * @param patientId 患者ID
     * @param files     影像文件列表
     * @return 报告信息
     */
    @PostMapping("/generate")
    public ResultMessage<Report> generate(@NotBlank(message = "患者ID不能为空") @RequestParam String patientId,
                                          @RequestParam("files") List<MultipartFile> files) {
        AuthUser authUser = UserContext.getCurrentUser();
        String doctorId = authUser.getId();
        return ResultUtil.data(reportService.generateReport(doctorId, patientId, files));
    }

    /**
     * 根据ID获取报告
     *
     * @param id 报告ID
     * @return 报告信息
     */
    @GetMapping("/get/{id}")
    public ResultMessage<Report> get(@PathVariable String id) {
        return ResultUtil.data(reportService.getById(id));
    }

    /**
     * 根据患者ID获取报告列表（历史记录）
     *
     * @param patientId 患者ID
     * @return 报告列表
     */
    @GetMapping("/list/patient/{patientId}")
    public ResultMessage<List<ReportVO>> getByPatient(@PathVariable String patientId) {
        return ResultUtil.data(reportService.getByPatientId(patientId));
    }

    /**
     * 获取当前医生的所有报告
     *
     * @return 报告列表
     */
    @GetMapping("/list/mine")
    public ResultMessage<List<ReportVO>> getMyReports() {
        AuthUser authUser = UserContext.getCurrentUser();
        String doctorId = authUser.getId();
        return ResultUtil.data(reportService.getByDoctorId(doctorId));
    }

    /**
     * 更新报告内容（医生编辑）
     *
     * @param id             报告ID
     * @param reportContent  修改后的报告内容
     * @return 报告信息
     */
    @PutMapping("/content/{id}")
    public ResultMessage<Report> updateContent(@PathVariable String id,
                                               @NotBlank(message = "报告内容不能为空") @RequestParam String reportContent) {
        return ResultUtil.data(reportService.updateContent(id, reportContent, UserContext.getCurrentUserId()));
    }

    /**
     * 更新报告状态（确认/签发）
     *
     * @param id     报告ID
     * @param status 状态：CONFIRMED-已确认 SIGNED-已签发
     * @return 报告信息
     */
    @PutMapping("/status/{id}")
    public ResultMessage<Report> updateStatus(@PathVariable String id,
                                              @NotBlank(message = "状态不能为空") @RequestParam String status) {
        return ResultUtil.data(reportService.updateStatus(id, status,UserContext.getCurrentUserId()));
    }
}