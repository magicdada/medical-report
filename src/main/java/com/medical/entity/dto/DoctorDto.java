//package com.medical.entity.dto;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.medical.common.BaseEntity;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Table;
//
///**
// * 医生表
// * @author wangda
// * @since 2026/08/08
// */
//@EqualsAndHashCode(callSuper = true)
//@Data
//@Entity
//@Table(name = "doctor")
//public class DoctorDto extends BaseEntity {
//
//    private static final long serialVersionUID = 1L;
//
//    @Column(nullable = false, unique = true, length = 50)
//    private String username;
//
//    @Column(nullable = false)
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
//    private String password;
//
//    @Column(name = "real_name", length = 50)
//    private String realName;
//
//    @Column(length = 50)
//    private String department;
//
//    @Column(length = 20)
//    private String phone;
//
//    @Column(length = 100)
//    private String email;
//
//    @Column(columnDefinition = "BIT(1) DEFAULT 1")
//    private Boolean enabled = true;
//}