package com.springboot.springboothousemarket.Config;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.Util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket认证拦截器
 * 用于验证WebSocket连接和消息中的JWT令牌
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UsersService usersService;
    private final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    public WebSocketAuthInterceptor(JwtUtil jwtUtil, UsersService usersService) {
        this.jwtUtil = jwtUtil;
        this.usersService = usersService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // 处理CONNECT和SUBSCRIBE命令的认证
        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            // 从STOMP头中获取Authorization令牌
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            // 如果头部没有令牌，尝试从查询参数获取（用于SockJS）
            if (token == null) {
                String query = accessor.getFirstNativeHeader("query");
                if (query != null && query.contains("token=")) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (param.startsWith("token=")) {
                            token = param.substring(6);
                            break;
                        }
                    }
                }
            }

            if (token == null) {
                logger.warn("WebSocket连接尝试未提供认证令牌");
                throw new SecurityException("未提供认证令牌");
            }

            try {
                // 验证令牌
                String username = jwtUtil.extractUsername(token);
                if (username == null || jwtUtil.isTokenExpired(token)) {
                    logger.warn("WebSocket令牌无效或已过期");
                    throw new SecurityException("令牌无效或已过期");
                }

                // 从数据库获取用户信息
                Users user = usersService.getUserByUsername(username);
                if (user == null) {
                    logger.warn("WebSocket令牌对应的用户不存在: {}", username);
                    throw new SecurityException("用户不存在");
                }

                // 提取角色信息
                List<GrantedAuthority> authorities = jwtUtil.extractRoles(token).stream()
                        .map(role -> new SimpleGrantedAuthority(role))
                        .collect(Collectors.toList());

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                // 设置认证信息到STOMP头中
                accessor.setUser(authentication);

                logger.info("WebSocket用户认证成功: {}", username);

            } catch (Exception e) {
                logger.error("WebSocket认证失败: {}", e.getMessage());
                throw new SecurityException("认证失败: " + e.getMessage());
            }
        }

        return message;
    }
}