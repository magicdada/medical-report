package com.medical.mapper;

import com.medical.entity.dos.PatientChat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 患者扫码对话凭证数据处理层
 *
 * @author wangda
 * @since 2026/10/01
 */
public interface PatientChatMapper extends JpaRepository<PatientChat, String> {

    /**
     * 根据访问码查询凭证记录
     * @param accessCode 访问码
     * @return 凭证记录
     */
    PatientChat findByAccessCode(String accessCode);
}