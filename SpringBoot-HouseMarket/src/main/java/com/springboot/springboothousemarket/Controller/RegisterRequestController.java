package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.CaptchaService;
import com.springboot.springboothousemarket.Service.RegisterRequestService;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.Util.JwtUtil;
import com.springboot.springboothousemarket.dto.LoginRequest;
import com.springboot.springboothousemarket.dto.LoginResponse;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import com.springboot.springboothousemarket.dto.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class RegisterRequestController {

    private final RegisterRequestService service;
    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;
    private final UsersService usersService;

    public RegisterRequestController(RegisterRequestService service,
                                     CaptchaService captchaService,
                                     JwtUtil jwtUtil,
                                     UsersService usersService) {
        this.service = service;
        this.captchaService = captchaService;
        this.jwtUtil = jwtUtil;
        this.usersService = usersService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseResult> register(@RequestBody RegisterRequest request) {
        try {
            if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
                return ResponseEntity.badRequest().body(ResponseResult.fail("验证码错误或已过期"));
            }
            RegisterRequest user = new RegisterRequest();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setRole(request.getRole());
            user.setStatus("normal");
            service.register(user);
            return ResponseEntity.ok(ResponseResult.ok("注册成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResponseResult.fail("注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
                return ResponseEntity.badRequest().body(new LoginResponse(400, "验证码错误或已过期", null, null));
            }
            RegisterRequest tempUser = service.login(request.getUsername(), request.getPassword(), request.getRole());
            if (tempUser == null) {
                return ResponseEntity.badRequest().body(new LoginResponse(400, "登录失败", null, null));
            }

            Users fullUser = usersService.getUserByUsername(request.getUsername());
            if (fullUser == null) {
                return ResponseEntity.badRequest().body(new LoginResponse(400, "用户信息不存在", null, null));
            }

            String token = jwtUtil.generateToken(fullUser.getUsername(), fullUser.getRole());
            return ResponseEntity.ok(new LoginResponse(200, "登录成功", fullUser, token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new LoginResponse(400, e.getMessage(), null, null));
        }
    }

    @GetMapping("/captcha")
    public ResponseResult captcha() {
        return ResponseResult.ok(null, captchaService.generate());
    }
}
