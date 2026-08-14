package com.medical.mapper;

import com.medical.entity.dos.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 患者数据处理层
 *
 * @author wangda
 * @since 2026/08/08
 */
public interface PatientMapper extends JpaRepository<Patient, String> {

    /**
     * 根据姓名模糊查询患者列表
     *
     * @param name 患者姓名（模糊匹配）
     * @return 患者列表
     */
    List<Patient> findByNameContaining(String name);

    /**
     * 根据患者编号查询患者
     *
     * @param patientNo 患者编号
     * @return 患者信息
     */
    Patient findByPatientNo(String patientNo);
}