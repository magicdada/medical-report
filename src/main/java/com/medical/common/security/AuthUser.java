package com.medical.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 授权用户信息
 *
 * @author wangda
 * @since 2026/08/08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser implements Serializable {

    private static final long serialVersionUID = 582441893336003319L;

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 科室
     */
    private String department;

    public AuthUser(String username, String id, String realName) {
        this.username = username;
        this.id = id;
        this.realName = realName;
    }
}