package com.springboot.springboothousemarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private int code;
    private String msg;
    private Object data;
    private String token;
}
