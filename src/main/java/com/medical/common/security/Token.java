package com.medical.common.security;

import lombok.Data;

/**
 * Token实体类
 *
 * @author wangda
 * @since 2026/08/08
 */
@Data
public class Token {

    /**
     * 访问token
     */
    private String accessToken;

    /**
     * 刷新token
     */
    private String refreshToken;
}