package com.medical.config;

import com.medical.common.properties.IgnoredUrlsProperties;
import com.medical.common.security.CustomAccessDeniedHandler;
import com.medical.common.security.filter.JwtAuthenticationFilter;
import com.medical.common.util.ResponseUtil;
import com.medical.mapper.DoctorTokenMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 核心配置类
 * @author wangda
 * @since 2026/08/08
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private IgnoredUrlsProperties ignoredUrlsProperties;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private DoctorTokenMapper doctorTokenMapper;

    /**
     * 配置认证管理器，使用自定义JWT认证
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> {
            throw new UsernameNotFoundException("不支持此认证方式");
        });
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //配置的url 不需要授权
        for (String url : ignoredUrlsProperties.getUrls()) {
            http.authorizeRequests().antMatchers(url).permitAll();
        }

        http
                //禁止网页iframe
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                //任何请求
                .anyRequest()
                //需要身份认证
                .authenticated()
                .and()
                //允许跨域
                .cors().configurationSource(corsConfigurationSource)
                .and()
                //关闭跨站请求防护
                .csrf().disable()
                //前后端分离采用JWT 不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //自定义权限拒绝处理类
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint((request, response, authException) ->
                        ResponseUtil.output(response, 403,
                                ResponseUtil.resultMap(false, 403, "未登录或token失效")))
                .and()
                //禁用表单登录
                .formLogin().disable()
                //禁用Basic认证
                .httpBasic().disable()
                //添加JWT认证过滤器
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), doctorTokenMapper));
    }
}