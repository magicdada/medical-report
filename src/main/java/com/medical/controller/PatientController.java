package com.medical.controller;

import com.medical.common.ResultMessage;
import com.medical.common.util.ResultUtil;
import com.medical.entity.dos.Patient;
import com.medical.service.PatientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 患者接口
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * 新增患者
     *
     * @param patient 患者信息
     * @return 患者信息
     */
    @PostMapping("/add")
    public ResultMessage<Patient> add(@RequestBody Patient patient) {
        return ResultUtil.data(patientService.add(patient));
    }

    /**
     * 根据ID获取患者
     *
     * @param id 患者ID
     * @return 患者信息
     */
    @GetMapping("/get/{id}")
    public ResultMessage<Patient> get(@PathVariable String id) {
        return ResultUtil.data(patientService.getById(id));
    }

    /**
     * 根据姓名搜索患者
     *
     * @param name 姓名
     * @return 患者列表
     */
    @GetMapping("/search")
    public ResultMessage<List<Patient>> search(@NotBlank(message = "姓名不能为空") @RequestParam String name) {
        return ResultUtil.data(patientService.search(name));
    }

    /**
     * 获取所有患者
     *
     * @return 患者列表
     */
    @GetMapping("/list")
    public ResultMessage<List<Patient>> list() {
        return ResultUtil.data(patientService.getAll());
    }
}