package com.springboot.springboothousemarket.Util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类，用于处理JWT令牌的创建、解析和验证
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secret;

    private SecretKey getSignInKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("未配置 jwt.secret");
        }
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
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
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10小时过期
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
