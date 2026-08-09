package com.medical.service.impl;

import com.medical.entity.dos.Patient;
import com.medical.mapper.PatientMapper;
import com.medical.service.PatientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 患者业务层实现
 *
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public Patient add(Patient patient) {
        patientMapper.save(patient);
        log.info("新增患者：{}", patient.getName());
        return patient;
    }

    @Override
    public Patient getById(String id) {
        return patientMapper.findById(id).orElse(null);
    }

    @Override
    public List<Patient> search(String name) {
        return patientMapper.findByNameContaining(name);
    }

    @Override
    public List<Patient> getAll() {
        return patientMapper.findAll();
    }
}