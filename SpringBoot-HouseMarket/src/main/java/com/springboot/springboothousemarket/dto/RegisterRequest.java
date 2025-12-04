package com.springboot.springboothousemarket.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String confirmPassword;
    private String role; // 前端传：租客 / 房东
}
