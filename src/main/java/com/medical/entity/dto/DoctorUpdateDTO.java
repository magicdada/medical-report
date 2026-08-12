package com.medical.entity.dto;

import lombok.Data;

import javax.validation.constraints.Email;

/**
 * 医生信息更新DTO
 *
 * @author wangda
 * @since 2026/08/12
 */
@Data
public class DoctorUpdateDTO {

    /** 真实姓名 */
    private String realName;

    /** 科室 */
    private String department;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;
}