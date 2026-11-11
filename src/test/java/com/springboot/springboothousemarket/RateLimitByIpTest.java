package com.springboot.springboothousemarket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 限流的「IP 维度」验证。
 *
 * <p>背景：原来只按用户名限流，攻击者换一批用户名即可无限次撞库/批量注册，
 * 每个用户名都只触发一次计数，整体请求数不受约束。补上 IP 维度后：
 * <ul>
 *   <li>账号维度：请求前计数（成功也计数），防单账号爆破；</li>
 *   <li>IP 维度：登录只在「凭据错误」时计数 —— 成功登录不消耗额度，
 *       否则共享出口 IP 的办公网/校园网会被正常用户互相连坐误伤；
 *       但持续失败说明这个 IP 在撞库，此时整体拦下。</li>
 * </ul>
 *
 * <p>注意：本类使用独立的用户名与独立的 X-Forwarded-For（文档保留网段），
 * 避免与其它测试类共享 {@code InMemoryRateLimiter} 单例的计数状态。
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class RateLimitByIpTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 同一 IP 连续用不同用户名登录失败 → 达到阈值后被 IP 维度整体拦下。
     * 这是只按用户名限流时完全拦不住的场景（批量撞库枚举用户名）。
     */
    @Test
    void repeatedFailedLoginsFromSameIpAreBlocked() throws Exception {
        String ip = "203.0.113.77";
        for (int i = 0; i < 30; i++) {
            mockMvc.perform(login("rl_fail_" + i, "wrong-password", ip))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
        // 第 31 次失败：同一 IP 的失败次数超过阈值
        mockMvc.perform(login("rl_fail_over", "wrong-password", ip))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429));
    }

    /**
     * 账号维度依然生效：换 IP 也无法绕过「同一账号」的频率限制。
     */
    @Test
    void sameAccountIsBlockedEvenWhenIpChanges() throws Exception {
        String username = "rl_account_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(login(username, "wrong-password", "198.51.100." + (10 + i)))
                    .andExpect(status().isBadRequest());
        }
        // 第 6 次：即使换了 IP，账号维度依然超限
        mockMvc.perform(login(username, "wrong-password", "198.51.100.99"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429));
    }

    /**
     * 关键回归：成功登录不消耗 IP 额度。
     *
     * <p>如果 IP 维度在请求前计数（无论成败），那么同一出口 IP 下的
     * 正常用户登录若干次后就会集体被拒 —— 办公网里几十号人共用一个出口 IP，
     * 这是非常典型的「限流误伤」事故。这里用 6 个不同账号验证：
     * 6 次成功登录后仍然还能继续 30 次失败尝试。
     */
    @Test
    void successfulLoginsDoNotConsumeIpQuota() throws Exception {
        String ip = "198.51.100.200";
        for (String user : new String[]{"tenant1", "tenant2", "tenant3",
                "landlord1", "landlord2", "landlord3"}) {
            mockMvc.perform(login(user, "123456", ip))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
        // 成功登录没有占用 IP 额度，失败额度仍然完整
        for (int i = 0; i < 30; i++) {
            mockMvc.perform(login("rl_after_ok_" + i, "wrong-password", ip))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(login("rl_after_ok_over", "wrong-password", ip))
                .andExpect(status().isTooManyRequests());
    }

    private MockHttpServletRequestBuilder login(String username, String password, String ip) {
        return post("/api/v1/auth/login")
                .header("X-Forwarded-For", ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
    }
}
