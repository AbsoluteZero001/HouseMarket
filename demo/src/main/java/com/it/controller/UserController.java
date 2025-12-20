package com.it.controller;

import com.it.pojo.User;
import com.it.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            // 创建User对象
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            // 根据角色设置role字段
            user.setRole(request.getRole());
            // 设置默认状态
            user.setStatus("active");
            
            // 调用service层注册
            userService.register(user);
            
            return ResponseEntity.ok(new ResponseResult(true, "注册成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseResult(false, "注册失败：" + e.getMessage()));
        }
    }
    
    // 注册请求参数类
    static class UserRegisterRequest {
        private String username;
        private String password;
        private String confirmPassword;
        private String role;
        
        // getter和setter
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
        public String getConfirmPassword() {
            return confirmPassword;
        }
        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
        public String getRole() {
            return role;
        }
        public void setRole(String role) {
            this.role = role;
        }
    }
    
    // 响应结果类
    static class ResponseResult {
        private boolean success;
        private String message;
        
        public ResponseResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // getter
        public boolean isSuccess() {
            return success;
        }
        public String getMessage() {
            return message;
        }
    }
}