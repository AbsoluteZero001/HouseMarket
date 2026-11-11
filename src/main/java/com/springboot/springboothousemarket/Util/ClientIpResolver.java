package com.springboot.springboothousemarket.Util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 客户端 IP 解析：限流要按 IP 维度计数，必须拿到真实的来源地址。
 *
 * 坑点：应用部署在 Nginx / 网关之后时，request.getRemoteAddr() 拿到的是代理地址，
 * 所有用户会共用同一个 IP 桶 —— IP 维度的限流会误伤全部正常用户。
 * 因此优先读取 X-Forwarded-For 的第一段（最靠近客户端的那个地址），
 * 并可通过 app.ratelimit.trust-proxy-headers 关闭该行为（应用直接对外暴露时必须关闭）。
 */
@Component
public class ClientIpResolver {

    @Value("${app.ratelimit.trust-proxy-headers:true}")
    private boolean trustProxyHeaders;

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        if (trustProxyHeaders) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                // XFF 形如 "client, proxy1, proxy2"，第一段是最原始的客户端地址
                int comma = forwarded.indexOf(',');
                String first = (comma > 0 ? forwarded.substring(0, comma) : forwarded).trim();
                if (!first.isEmpty()) {
                    return normalize(first);
                }
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return normalize(realIp.trim());
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? "unknown" : normalize(remote);
    }

    /**
     * 归一化地址：把 IPv6 回环与 IPv4 映射地址折叠到同一形式，
     * 避免 ::1 与 127.0.0.1 被当成两个不同的来源绕过限流。
     */
    private String normalize(String ip) {
        if ("::1".equals(ip)) {
            return "127.0.0.1";
        }
        if (ip.startsWith("::ffff:")) {
            return ip.substring("::ffff:".length());
        }
        return ip;
    }
}
