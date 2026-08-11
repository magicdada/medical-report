package com.medical.service;

import com.medical.common.security.Token;
import com.medical.entity.dos.Doctor;

/**
 * 医生业务层
 * @author wangda
 * @since 2026/08/08
 */
public interface DoctorService {

    /**
     * 医生注册
     *
     * @param username   用户名
     * @param password   密码
     * @param realName   真实姓名
     * @param department 科室
     * @return 医生信息
     */
    Doctor register(String username, String password, String realName, String department);

    /**
     * 医生登录
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    Token login(String username, String password);

    /**
     * 根据ID获取医生信息
     *
     * @param id 医生ID
     * @return 医生信息
     */
    Doctor getById(String id);

    /**
     * 根据用户名获取医生信息
     *
     * @param username 用户名
     * @return 医生信息
     */
    Doctor getByUsername(String username);

    /**
     * 退出登录
     *
     * @param doctorId 医生ID
     */
    void logout(String doctorId);
}