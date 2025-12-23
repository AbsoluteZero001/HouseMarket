package com.springboot.springboothousemarket.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置类
 * 用于配置Spring Security的安全策略
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    /**
     * 构造函数，注入JWT过滤器
     *
     * @param jwtFilter JWT过滤器
     */
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * 配置认证管理器Bean
     *
     * @param config 认证配置
     * @return 认证管理器
     * @throws Exception 可能抛出的异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置安全过滤器链
     *
     * @param http HTTP安全配置
     * @return 安全过滤器链
     * @throws Exception 可能抛出的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用 CSRF（前后端分离项目一般禁用）
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 放行注册、登录接口
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 放行验证码接口
                        .requestMatchers("/captcha").permitAll()
                        // 放行首页、静态资源、Swagger 文档
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/register.html",
                                "/login.html",
                                "/HouseMarket/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/webjars/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // 其他请求必须认证
                        .anyRequest().authenticated()
                )

                // 3. 配置会话管理为无状态
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4. 在用户名密码认证过滤器之前加入 JWT 过滤器
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}