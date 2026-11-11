package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.RateLimiter;
import com.springboot.springboothousemarket.Service.RegisterRequestService;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.Util.ClientIpResolver;
import com.springboot.springboothousemarket.Util.JwtUtil;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.LoginFailedException;
import com.springboot.springboothousemarket.dto.LoginRequest;
import com.springboot.springboothousemarket.dto.LoginResponse;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import com.springboot.springboothousemarket.dto.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class RegisterRequestController {

    private final RegisterRequestService service;
    private final JwtUtil jwtUtil;
    private final UsersService usersService;
    private final RateLimiter rateLimiter;
    private final ClientIpResolver clientIpResolver;

    @Value("${app.ratelimit.window-seconds:60}")
    private int windowSeconds;

    @Value("${app.ratelimit.login-user-limit:5}")
    private int loginUserLimit;

    @Value("${app.ratelimit.login-ip-limit:30}")
    private int loginIpLimit;

    @Value("${app.ratelimit.register-user-limit:5}")
    private int registerUserLimit;

    @Value("${app.ratelimit.register-ip-limit:20}")
    private int registerIpLimit;

    public RegisterRequestController(RegisterRequestService service,
                                     JwtUtil jwtUtil,
                                     UsersService usersService,
                                     RateLimiter rateLimiter,
                                     ClientIpResolver clientIpResolver) {
        this.service = service;
        this.jwtUtil = jwtUtil;
        this.usersService = usersService;
        this.rateLimiter = rateLimiter;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseResult> register(@RequestBody RegisterRequest request,
                                                   HttpServletRequest httpRequest) {
        String username = normalizeUsername(request.getUsername());
        if (!acquireRegister(username, clientIpResolver.resolve(httpRequest))) {
            // 限流用 429（HTTP 与 body.code 保持一致），与登录接口保持同样的语义
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ResponseResult.fail("注册尝试过于频繁，请稍后再试"));
        }
        RegisterRequest user = new RegisterRequest();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setRole(request.getRole());
        user.setStatus("normal");
        // 这里刻意不写 catch(Exception)：旧实现把注册失败与系统异常一起吞成
        // 「注册失败: xxx」，既丢失了业务提示，也让真实故障无法定位。
        // BusinessException 交由 GlobalExceptionHandler 统一转成 400 + {success:false,message,code}。
        service.register(user);
        return ResponseEntity.ok(ResponseResult.ok("注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        String username = normalizeUsername(request.getUsername());
        String clientIp = clientIpResolver.resolve(httpRequest);

        // 维度一：账号（用户名）——请求前计数。
        // 保持既有语义不变：无论登录成功还是失败都计入，防止针对单一账号的爆破。
        if (!tryAcquireUser("login", username)) {
            // 限流用 429（HTTP 与 body.code 保持一致），不再伪装成 400
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new LoginResponse(429, "登录尝试过于频繁，请稍后再试", null, null));
        }

        RegisterRequest tempUser;
        try {
            tempUser = service.login(request.getUsername(), request.getPassword(), request.getRole());
        } catch (LoginFailedException e) {
            // 维度二：来源 IP——只在「凭据错误」时计数。
            // 成功登录不消耗 IP 额度：办公网/学校/网吧共用一个出口 IP，
            // 如果请求前就计数，正常的多人登录会被整个误伤；
            // 而持续失败才说明这个 IP 在撞库（枚举一批用户名），此时再整体拦下。
            if (!tryAcquireIp("login", clientIp)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new LoginResponse(429, "登录失败次数过多，请稍后再试", null, null));
            }
            return ResponseEntity.badRequest().body(new LoginResponse(400, e.getMessage(), null, null));
        } catch (BusinessException e) {
            // 其余可预期的业务错误（账号被禁用等）→ 400 + 明确文案，
            // 不计入 IP 维度：这类错误与撞库无关，牵连共享 IP 只会造成误伤。
            return ResponseEntity.badRequest().body(new LoginResponse(400, e.getMessage(), null, null));
        }
        if (tempUser == null) {
            if (!tryAcquireIp("login", clientIp)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new LoginResponse(429, "登录失败次数过多，请稍后再试", null, null));
            }
            return ResponseEntity.badRequest().body(new LoginResponse(400, "登录失败", null, null));
        }

        Users fullUser = usersService.getUserByUsername(request.getUsername());
        if (fullUser == null) {
            return ResponseEntity.badRequest().body(new LoginResponse(400, "用户信息不存在", null, null));
        }

        String token = jwtUtil.generateToken(fullUser.getUsername(), fullUser.getRole());
        return ResponseEntity.ok(new LoginResponse(200, "登录成功", fullUser, token));
    }

    /**
     * 注册限流判定：用户名维度 + 客户端 IP 维度同时检查，任一超限即拒绝。
     *
     * 注册场景两个维度都在请求前计数 —— 批量注册本身就是滥用行为，
     * 正常用户不会在 1 分钟内反复注册多个账号，没有误伤风险。
     */
    private boolean acquireRegister(String username, String clientIp) {
        return tryAcquireUser("register", username) && tryAcquireIp("register", clientIp);
    }

    /** 用户名维度计数。 */
    private boolean tryAcquireUser(String scope, String username) {
        return rateLimiter.tryAcquire(scope + ":user:" + username, userLimitOf(scope), window());
    }

    /** 客户端 IP 维度计数。 */
    private boolean tryAcquireIp(String scope, String clientIp) {
        return rateLimiter.tryAcquire(scope + ":ip:" + clientIp, ipLimitOf(scope), window());
    }

    private Duration window() {
        return Duration.ofSeconds(Math.max(windowSeconds, 1));
    }

    private int userLimitOf(String scope) {
        return "login".equals(scope) ? loginUserLimit : registerUserLimit;
    }

    private int ipLimitOf(String scope) {
        return "login".equals(scope) ? loginIpLimit : registerIpLimit;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }
}
