package com.springboot.springboothousemarket.dto;

/**
 * 登录凭据错误（用户名或密码不匹配）。
 *
 * <p>与普通 {@link BusinessException} 区分开，是为了让限流策略能区分两类失败：
 * <ul>
 *   <li>凭据错误 = 可被用于撞库枚举 → 需要计入「来源 IP 维度」的失败计数；</li>
 *   <li>账号被禁用等状态类业务错误 = 正常用户也会遇到 → 不应计入 IP 维度，
 *       否则共享出口 IP 的整个办公网会被误伤。</li>
 * </ul>
 * 它继承 {@code BusinessException}，因此全局异常处理器无需改动，响应契约保持不变。
 */
public class LoginFailedException extends BusinessException {

    public LoginFailedException(String message) {
        super(message);
    }
}
