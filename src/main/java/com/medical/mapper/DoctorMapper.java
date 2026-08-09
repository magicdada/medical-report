package com.medical.mapper;

import com.medical.entity.dos.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 医生数据处理层
 * @author wangda
 * @since 2026/08/08
 */
public interface DoctorMapper extends JpaRepository<Doctor,String> {

    Doctor findByUsername(String username);

    boolean existsByUsername(String username);
}
