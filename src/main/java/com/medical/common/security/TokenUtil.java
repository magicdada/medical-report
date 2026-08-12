package com.medical.common.security;

import com.medical.common.ResultCode;
import com.medical.common.ServiceException;
import com.medical.common.enums.SecurityEnum;
import com.medical.entity.dos.DoctorToken;
import com.medical.mapper.DoctorTokenMapper;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Token工具类
 *
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
@Component
public class TokenUtil {

    /**
     * accessToken过期时间（分钟）
     */
    private static final long TOKEN_EXPIRE_TIME = 60 * 24;

    /**
     * refreshToken过期时间倍数
     */
    private static final long REFRESH_EXPIRE_MULTIPLE = 2;

    @Autowired
    private DoctorTokenMapper doctorTokenMapper;

    /**
     * 创建token
     *
     * @param authUser 用户信息
     * @return Token
     */
    @Transactional(rollbackFor = Exception.class)
    public Token createToken(AuthUser authUser) {
        Token token = new Token();

        // 生成accessToken
        String accessToken = createToken(authUser, TOKEN_EXPIRE_TIME);
        // 生成refreshToken，过期时间为accessToken的2倍
        String refreshToken = createToken(authUser, TOKEN_EXPIRE_TIME * REFRESH_EXPIRE_MULTIPLE);

        // 删除旧token
        doctorTokenMapper.deleteByDoctorId(authUser.getId());

        // 保存新token到数据库
        DoctorToken doctorToken = new DoctorToken();
        doctorToken.setDoctorId(authUser.getId());
        doctorToken.setAccessToken(accessToken);
        doctorToken.setRefreshToken(refreshToken);
        doctorToken.setExpireTime(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_TIME * 60 * 1000));
        doctorToken.setRefreshExpireTime(new Date(System.currentTimeMillis() + TOKEN_EXPIRE_TIME * REFRESH_EXPIRE_MULTIPLE * 60 * 1000));
        doctorTokenMapper.save(doctorToken);

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        return token;
    }

    /**
     * 刷新token
     *
     * @param oldRefreshToken 旧的刷新token
     * @return 新的Token
     */
    @Transactional(rollbackFor = Exception.class)
    public Token refreshToken(String oldRefreshToken) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(SecretKeyUtil.generalKey())
                    .build()
                    .parseSignedClaims(oldRefreshToken)
                    .getPayload();
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new ServiceException(ResultCode.USER_AUTH_EXPIRED);
        }

        // 获取存储在claims中的用户信息
        String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
        AuthUser authUser = new Gson().fromJson(json, AuthUser.class);

        // 验证数据库中是否存在该refreshToken
        DoctorToken doctorToken = doctorTokenMapper.findByRefreshToken(oldRefreshToken);
        if (doctorToken == null) {
            throw new ServiceException(ResultCode.USER_AUTH_EXPIRED);
        }

        // 生成新的双token
        return createToken(authUser);
    }

    /**
     * 生成token
     *
     * @param authUser       用户信息
     * @param expirationTime 过期时间（分钟）
     * @return token字符串
     */
    private String createToken(AuthUser authUser, Long expirationTime) {
        return Jwts.builder()
                .claim(SecurityEnum.USER_CONTEXT.getValue(), new Gson().toJson(authUser))
                .subject(authUser.getUsername())
                .expiration(new Date(System.currentTimeMillis() + expirationTime * 60 * 1000))
                .signWith(SecretKeyUtil.generalKey())
                .compact();
    }
}