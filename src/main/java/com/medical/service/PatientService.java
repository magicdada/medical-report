package com.medical.service;

import com.medical.entity.dos.Patient;

import java.util.List;

/**
 * 患者业务层
 * @author wangda
 * @since 2026/08/08
 */
public interface PatientService {

    /**
     * 新增患者
     *
     * @param patient 患者信息
     * @return 患者信息
     */
    Patient add(Patient patient);

    /**
     * 根据ID获取患者
     *
     * @param id 患者ID
     * @return 患者信息
     */
    Patient getById(String id);

    /**
     * 根据姓名模糊搜索
     *
     * @param name 姓名
     * @return 患者列表
     */
    List<Patient> search(String name);

    /**
     * 获取所有患者
     *
     * @return 患者列表
     */
    List<Patient> getAll();
}