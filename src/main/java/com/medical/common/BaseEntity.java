package com.medical.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 数据库基础实体类
 * @author wangda
 * @since 2026/08/08
 * */
@Data
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 32)
    private String id;

    @Column(name = "create_by", length = 50)
    private String createBy;

    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_by", length = 50)
    private String updateBy;

    @JsonFormat(timezone = "GMT+10", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "delete_flag")
    private Boolean deleteFlag;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString().replace("-", "");
        }
        this.createTime = new Date();
        this.updateTime = new Date();
        if (this.deleteFlag == null) {
            this.deleteFlag = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = new Date();
    }
}