package com.springboot.springboothousemarket.Config;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT过滤器组件
 * 用于验证和处理HTTP请求中的JWT令牌
 */
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UsersService usersService;
    private final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    public JwtFilter(JwtUtil jwtUtil, UsersService usersService) {
        this.jwtUtil = jwtUtil;
        this.usersService = usersService;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // 跳过WebSocket请求，直接放行
        if (request.getRequestURI().startsWith("/ws")) {
            chain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        // 1. 如果请求中没有 JWT，直接放行（正常情况）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        // 从请求头中提取 Token
        String jwtToken = authHeader.substring(7);
        String username;
        try {
            // 2. 提取用户名
            username = jwtUtil.extractUsername(jwtToken);

            // 3. 检查 Token 是否过期
            if (username != null && jwtUtil.isTokenExpired(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 返回 401
                response.getWriter().write("Token has expired");
                return;
            }
        } catch (Exception e) {
            // Token 非法 / 解析失败
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 返回 401
            response.getWriter().write("Invalid Token: " + e.getMessage());
            return;
        }
        // 4. 如果用户名不为空，并且当前没有认证，才注入认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 从数据库中获取用户信息
            Users user = usersService.getUserByUsername(username);
            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 返回 401
                response.getWriter().write("User not found");
                return;
            }
            // 从 JWT 中提取角色信息，并转换为 GrantedAuthority 对象
            List<GrantedAuthority> authorities = jwtUtil.extractRoles(jwtToken).stream()
                    .map(role -> new SimpleGrantedAuthority(role)) // 转换为 GrantedAuthority
                    .collect(Collectors.toList());
            // 创建认证信息对象，使用 Users 对象作为 Principal
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                    authorities);
            // 设置认证信息到 Spring Security 上下文
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 打印日志
            logger.info("Authenticated user: " + username);
        }
        // 执行后续的过滤器链
        chain.doFilter(request, response);
    }
}
