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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT过滤器组件
 * 用于验证和处理HTTP请求中的JWT令牌
 * 注意：此类不标注 @Component，由 SecurityConfig 手动创建，
 * 避免被 Spring Boot 自动注册为全局 Servlet Filter
 */
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

        // WebSocket握手请求也需要认证，但认证方式不同
        // WebSocket连接将在WebSocketAuthInterceptor中认证
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
            // 1. 提取用户名
            username = jwtUtil.extractUsername(jwtToken);

            // 2. 检查 Token 是否过期：明确返回 401，而不是静默降级为匿名
            if (username != null && jwtUtil.isTokenExpired(jwtToken)) {
                logger.warn("Rejecting expired JWT for request: {}", request.getRequestURI());
                writeUnauthorized(response, "登录状态已过期，请重新登录");
                return;
            }
        } catch (Exception e) {
            // Token 非法 / 解析失败：明确返回 401
            logger.warn("Rejecting invalid JWT for request: {}", request.getRequestURI());
            writeUnauthorized(response, "无效的登录凭证，请重新登录");
            return;
        }

        // 3. 如果用户名不为空，并且当前没有认证，才注入认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 从数据库中获取用户信息（每次请求实时校验账号存在与状态，禁用账号立即失去业务权限）
            Users user = usersService.getUserByUsername(username);
            if (user == null) {
                logger.warn("Rejecting JWT for missing user: {}", username);
                writeUnauthorized(response, "账号不存在或已被删除，请重新登录");
                return;
            }
            if (!"normal".equals(user.getStatus())) {
                logger.warn("Rejecting JWT for disabled user: {}", username);
                writeUnauthorized(response, "账号已被禁用，请联系管理员");
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

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\",\"code\":401}");
    }
}
