package com.medical.service.impl;

import com.medical.common.ResultCode;
import com.medical.common.ServiceException;
import com.medical.common.security.AuthUser;
import com.medical.common.security.Token;
import com.medical.common.security.TokenUtil;
import com.medical.entity.dos.Doctor;
import com.medical.mapper.DoctorMapper;
import com.medical.service.DoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 医生业务层实现
 *
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Doctor register(String username, String password, String realName, String department) {
        if (doctorMapper.existsByUsername(username)) {
            throw new ServiceException(ResultCode.USER_EXIST);
        }
        Doctor doctor = new Doctor();
        doctor.setUsername(username);
        doctor.setPassword(passwordEncoder.encode(password));
        doctor.setRealName(realName);
        doctor.setDepartment(department);
        doctor.setCreateBy(username);
        doctorMapper.save(doctor);
        log.info("医生注册成功：{}", username);
        return doctor;
    }

    @Override
    public Token login(String username, String password) {
        Doctor doctor = doctorMapper.findByUsername(username);
        if (doctor == null) {
            throw new ServiceException(ResultCode.USER_NOT_EXIST);
        }
        if (!passwordEncoder.matches(password, doctor.getPassword())) {
            throw new ServiceException(ResultCode.USER_PASSWORD_ERROR);
        }
        if (!doctor.getEnabled()) {
            throw new ServiceException(ResultCode.USER_STATUS_ERROR);
        }

        // 构造AuthUser
        AuthUser authUser = AuthUser.builder()
                .id(doctor.getId())
                .username(doctor.getUsername())
                .realName(doctor.getRealName())
                .department(doctor.getDepartment())
                .build();

        log.info("医生登录成功：{}", username);
        // 生成双token
        return tokenUtil.createToken(authUser);
    }

    @Override
    public Doctor getById(String id) {
        return doctorMapper.findById(id).orElse(null);
    }

    @Override
    public Doctor getByUsername(String username) {
        return doctorMapper.findByUsername(username);
    }
}