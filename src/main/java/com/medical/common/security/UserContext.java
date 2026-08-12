package com.medical.common.security;

import com.medical.common.enums.SecurityEnum;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户上下文
 * @author wangda
 * @since 2026/08/08
 */
public class UserContext {

    /**
     * 根据request获取当前用户
     *
     * @return 授权用户
     */
    public static AuthUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof AuthUser) {
            return (AuthUser) authentication.getDetails();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        AuthUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户token
     *
     * @return token
     */
    public static String getCurrentUserToken() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getHeader(SecurityEnum.HEADER_TOKEN.getValue());
        }
        return null;
    }

    /**
     * 根据token获取用户信息
     *
     * @param accessToken token
     * @return 授权用户
     */
    public static AuthUser getAuthUser(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SecretKeyUtil.generalKey())
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
            String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
            return new Gson().fromJson(json, AuthUser.class);
        } catch (Exception e) {
            return null;
        }
    }
}