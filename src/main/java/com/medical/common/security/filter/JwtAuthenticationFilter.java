package com.medical.common.security.filter;

import com.medical.common.security.AuthUser;
import com.medical.common.enums.SecurityEnum;
import com.medical.common.security.SecretKeyUtil;
import com.medical.common.util.ResponseUtil;
import com.medical.mapper.DoctorTokenMapper;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    private final DoctorTokenMapper doctorTokenMapper;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   DoctorTokenMapper doctorTokenMapper) {
        super(authenticationManager);
        this.doctorTokenMapper = doctorTokenMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // 从header中获取token
        String jwt = request.getHeader(SecurityEnum.HEADER_TOKEN.getValue());
        // 如果没有token则放行（交给Security配置决定是否拦截）
        if (jwt == null || jwt.trim().isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        // 获取用户信息，存入SecurityContext
        UsernamePasswordAuthenticationToken authentication = getAuthentication(jwt, response);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    /**
     * 解析token获取认证信息
     * @param jwt      token
     * @param response 响应
     * @return 认证信息
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String jwt, HttpServletResponse response) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SecretKeyUtil.generalKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
            // 获取存储在claims中的用户信息
            String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
            AuthUser authUser = new Gson().fromJson(json, AuthUser.class);

            // 验证数据库中是否存在该token
            if (doctorTokenMapper.findByAccessToken(jwt) != null) {
                List<GrantedAuthority> auths = new ArrayList<>();
                auths.add(new SimpleGrantedAuthority("ROLE_DOCTOR"));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUser.getUsername(), null, auths);
                authentication.setDetails(authUser);
                return authentication;
            }
            ResponseUtil.output(response, 403, ResponseUtil.resultMap(false, 403, "登录已失效，请重新登录"));
            return null;
        } catch (ExpiredJwtException e) {
            log.debug("token已过期:", e);
            ResponseUtil.output(response, 403, ResponseUtil.resultMap(false, 403, "登录已过期，请重新登录"));
        } catch (Exception e) {
            log.error("token解析异常:", e);
        }
        return null;
    }
}