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

    DoctorToken findByAccessToken(String accessToken);

    DoctorToken findByRefreshToken(String refreshToken);

    DoctorToken findByDoctorId(String doctorId);

    void deleteByDoctorId(@Param("doctorId") String doctorId);
}