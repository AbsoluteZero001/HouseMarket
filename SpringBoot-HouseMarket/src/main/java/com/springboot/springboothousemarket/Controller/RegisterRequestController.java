package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Service.CaptchaService;
import com.springboot.springboothousemarket.Service.RegisterRequestService;
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

    public RegisterRequestController(RegisterRequestService service, CaptchaService captchaService) {
        this.service = service;
        this.captchaService = captchaService;
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
            RegisterRequest user = service.login(request.getUsername(), request.getPassword(), request.getRole());
            String token = "token_" + System.currentTimeMillis();
            return ResponseEntity.ok(new LoginResponse(200, "登录成功", user, token));
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
