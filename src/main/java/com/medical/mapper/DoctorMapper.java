package com.medical.mapper;

import com.medical.entity.dos.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 医生数据处理层
 *
 * @author wangda
 * @since 2026/08/08
 */
public interface DoctorMapper extends JpaRepository<Doctor, String> {

    /**
     * 根据用户名查询医生
     *
     * @param username 用户名
     * @return 医生信息
     */
    Doctor findByUsername(String username);

    /**
     * 判断用户名是否已存在
     *
     * @param username 用户名
     * @return true-已存在 false-不存在
     */
    boolean existsByUsername(String username);
}