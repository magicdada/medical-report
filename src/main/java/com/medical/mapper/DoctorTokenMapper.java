package com.medical.mapper;

import com.medical.entity.dos.DoctorToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * 医生token数据处理层
 *
 * @author wangda
 * @since 2026/08/08
 */
public interface DoctorTokenMapper extends JpaRepository<DoctorToken, String> {

    /**
     * 根据访问令牌查询token记录
     *
     * @param accessToken 访问令牌
     * @return token记录
     */
    DoctorToken findByAccessToken(String accessToken);

    /**
     * 根据刷新令牌查询token记录
     *
     * @param refreshToken 刷新令牌
     * @return token记录
     */
    DoctorToken findByRefreshToken(String refreshToken);

    /**
     * 根据医生ID查询token记录
     *
     * @param doctorId 医生ID
     * @return token记录
     */
    DoctorToken findByDoctorId(String doctorId);

    /**
     * 根据医生ID删除token记录
     *
     * @param doctorId 医生ID
     */
    void deleteByDoctorId(@Param("doctorId") String doctorId);
}