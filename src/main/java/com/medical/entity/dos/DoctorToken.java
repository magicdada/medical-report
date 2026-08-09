package com.medical.entity.dos;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 医生token表
 *
 * @author wangda
 * @since 2026/08/08
 */
@Data
@Entity
@Table(name = "doctor_token")
public class DoctorToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "doctor_id", nullable = false, length = 32)
    private String doctorId;

    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false)
    private String refreshToken;

    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "expire_time", nullable = false)
    private Date expireTime;

    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "refresh_expire_time", nullable = false)
    private Date refreshExpireTime;

    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private Date createTime;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString().replace("-", "");
        }
        this.createTime = new Date();
    }
}