package com.springboot.springboothousemarket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String role; // 前端传：租客 / 房东
    private String status;
    private Long id;
    private Integer isDeleted;
}
