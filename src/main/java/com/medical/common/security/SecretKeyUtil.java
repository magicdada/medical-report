package com.medical.common.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

/**
 * 密钥工具类
 * @author wangda
 * @since 2026/08/08
 */
public class SecretKeyUtil {

    private static final String SECRET = "bWVkaWNhbC1yZXBvcnQtZ2VuZXJhdGUtc3lzdGVtLWp3dC1zZWNyZXQta2V5";

    public static SecretKey generalKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}