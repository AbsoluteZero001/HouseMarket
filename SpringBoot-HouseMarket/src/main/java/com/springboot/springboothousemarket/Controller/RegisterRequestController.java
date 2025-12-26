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
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
public class RegisterRequestController {

    private final RegisterRequestService service;
    private final CaptchaService captchaService;
    private final JwtUtil jwtUtil;
    private final UsersService sysUserService;

    public RegisterRequestController(RegisterRequestService service, CaptchaService captchaService, JwtUtil jwtUtil, UsersService sysUserService) {
        this.service = service;
        this.captchaService = captchaService;
        this.jwtUtil = jwtUtil;
        this.sysUserService = sysUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseResult> register(@RequestBody RegisterRequest request) {
        try {
            RegisterRequest user = new RegisterRequest(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole(),
                    "ACTIVE"
            );
            service.register(user);
            return ResponseEntity.ok(new ResponseResult(true, "注册成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseResult(false, "注册失败: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // 验证用户登录
            RegisterRequest tempUser = service.login(request.getUsername(), request.getPassword(), request.getRole());
            if (tempUser == null) {
                return ResponseEntity.badRequest().body(new LoginResponse(400, "登录失败", null, null));
            }

            // 获取完整的用户信息（包含ID）
            Users fullUser = sysUserService.getUserByUsername(request.getUsername());
            if (fullUser == null) {
                return ResponseEntity.badRequest().body(new LoginResponse(400, "用户信息不存在", null, null));
            }

            String token = jwtUtil.generateToken(fullUser.getUsername(), fullUser.getRole());
            return ResponseEntity.ok(new LoginResponse(200, "登录成功", fullUser, token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new LoginResponse(400, e.getMessage(), null, null));
        }
    }

    @GetMapping("/show")
    public ResponseEntity<RegisterRequest> show() {
        return ResponseEntity.ok(service.show());
    }

    @GetMapping("/captcha")
    public void captcha(HttpServletResponse response) throws IOException {
        BufferedImage image = captchaService.createCaptcha(); // 生成验证码
        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
    }

}