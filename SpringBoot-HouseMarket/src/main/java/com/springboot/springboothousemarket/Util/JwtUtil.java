package com.springboot.springboothousemarket.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类，用于处理JWT令牌的创建、解析和验证。
 *
 * 密钥策略：必须通过环境变量 JWT_SECRET 提供生产密钥；
 * 未配置时（仅限本地开发）生成一次性随机密钥并打印告警 —— 重启后所有已签发 token 失效，
 * 生产环境若不配置将在日志中看到明确告警，请务必配置。
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration-hours:10}")
    private long expirationHours;

    private SecretKey signInKey;

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank()) {
            byte[] random = new byte[64];
            new SecureRandom().nextBytes(random);
            String generated = Base64.getEncoder().encodeToString(random);
            signInKey = Keys.hmacShaKeyFor(generated.getBytes(StandardCharsets.UTF_8));
            log.warn("未配置 JWT_SECRET 环境变量，已使用一次性随机密钥（重启后所有登录态失效）。"
                    + "生产环境请务必通过环境变量 JWT_SECRET 提供固定密钥。");
        } else {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET 强度不足：Base64 解码后至少需要 32 字节（256 位）");
            }
            signInKey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    private SecretKey getSignInKey() {
        return signInKey;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long expirationMillis = expirationHours * 60 * 60 * 1000;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 提取角色信息
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);

        // 检查是否有新的 "roles" 字段（列表）
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        }

        // 向后兼容：检查旧的 "role" 字段（单个字符串）
        String role = claims.get("role", String.class);
        if (role != null) {
            return List.of(role);
        }

        // 如果没有角色信息，返回空列表
        return List.of();
    }
}
