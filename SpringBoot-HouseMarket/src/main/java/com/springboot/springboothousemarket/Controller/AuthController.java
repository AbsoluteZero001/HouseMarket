package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.SysUser;
import com.springboot.springboothousemarket.Service.SysUserService;
import com.springboot.springboothousemarket.Util.JwtUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证API")
@RequestMapping("/api")
@RestController
public class AuthController {

    private final SysUserService userService;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(SysUserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody SysUser user) {
        // 检查用户名是否已存在
        SysUser existingUser = userService.getUserByUsername(user.getUsername());
        if (existingUser != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户名已存在");
            return ResponseEntity.badRequest().body(response);
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsDeleted(0);

        // 创建用户
        SysUser createdUser = userService.createUser(user);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", createdUser.getId());
        userData.put("username", createdUser.getUsername());
        userData.put("role", createdUser.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "注册成功");
        response.put("data", userData);

        return ResponseEntity.ok(response);
    }

    /**
     * 用户登录
     *
     * @param loginUser 登录信息
     * @return 登录结果和JWT令牌
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginUser) {
        SysUser user = userService.getUserByUsername(loginUser.getUsername());

        // 检查用户是否存在且密码正确
        if (user == null || !passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return ResponseEntity.badRequest().body(response);
        }

        // 检查角色是否匹配
        if (!user.getRole().equals(loginUser.getRole())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户角色不匹配");
            return ResponseEntity.badRequest().body(response);
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("role", user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", userInfo);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登录成功");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    // 登录请求的数据传输对象
    static class LoginRequest {
        private String username;
        private String password;
        private String role;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}