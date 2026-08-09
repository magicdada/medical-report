package com.medical.mapper;

import com.medical.entity.dos.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 患者数据处理层
 * @author wangda
 * @since 2026/08/08
 */
public interface PatientMapper extends JpaRepository<Patient, String> {

    List<Patient> findByNameContaining(String name);

    Patient findByPatientNo(String patientNo);
}