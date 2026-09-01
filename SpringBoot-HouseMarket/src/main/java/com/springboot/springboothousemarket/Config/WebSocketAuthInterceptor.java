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

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket认证拦截器
 * 用于验证WebSocket连接和消息中的JWT令牌。
 *
 * 关键约定：STOMP 会话的 Principal name 统一为用户ID字符串，
 * 服务端 convertAndSendToUser(userId, ...) 依赖该约定做定向推送，
 * 前端统一订阅 /user/queue/...（由 STOMP 代理按会话隔离，无法越权订阅他人队列）。
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

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

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

                // 从数据库获取用户信息（同时校验账号状态）
                Users user = usersService.getUserByUsername(username);
                if (user == null) {
                    logger.warn("WebSocket令牌对应的用户不存在: {}", username);
                    throw new SecurityException("用户不存在");
                }
                if (!"normal".equals(user.getStatus())) {
                    logger.warn("WebSocket连接被拒绝，账号已禁用: {}", username);
                    throw new SecurityException("账号已被禁用");
                }

                // 提取角色信息
                List<GrantedAuthority> authorities = jwtUtil.extractRoles(token).stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Principal name 固定为用户ID字符串，与 convertAndSendToUser 的目标约定一致
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user.getId().toString(), null, authorities);

                accessor.setUser(authentication);

                logger.info("WebSocket用户认证成功: {} (userId={})", username, user.getId());

            } catch (SecurityException e) {
                throw e;
            } catch (Exception e) {
                logger.error("WebSocket认证失败: {}", e.getMessage());
                throw new SecurityException("认证失败");
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // 订阅白名单：只允许订阅 /user/...（STOMP 按会话用户隔离），防止猜 ID 越权订阅他人队列
            Principal principal = accessor.getUser();
            String destination = accessor.getDestination();
            if (principal == null) {
                throw new SecurityException("请先建立认证连接");
            }
            if (destination == null || !destination.startsWith("/user/")) {
                logger.warn("拒绝非法订阅 destination={}, user={}", destination, principal.getName());
                throw new SecurityException("非法的订阅目的地");
            }
        } else if (StompCommand.SEND.equals(accessor.getCommand())) {
            // 发送消息必须携带认证身份
            if (accessor.getUser() == null) {
                throw new SecurityException("请先建立认证连接");
            }
        }

        return message;
    }
}
