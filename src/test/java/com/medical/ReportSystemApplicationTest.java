package com.medical;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.medical.common.security.Token;
import com.medical.service.DoctorService;
import com.medical.service.PatientService;

/**
 * 系统功能测试
 * @author wangda
 * @since 2026/08/09
 */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReportSystemApplicationTest {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Test
    public void contextLoads() {
        log.info("Spring上下文加载成功");
    }

    @Test
    public void testLogin() {
        Token token = doctorService.login("doctor1", "123456");
        log.info("登录测试 - accessToken: {}", token.getAccessToken());
        log.info("登录测试 - refreshToken: {}", token.getRefreshToken());
    }
}