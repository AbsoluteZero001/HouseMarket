package com.springboot.springboothousemarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult {
    private boolean success;
    private String message;
    private Object data;

    public static ResponseResult ok(String message, Object data) {
        return new ResponseResult(true, message, data);
    }

    public static ResponseResult ok(String message) {
        return new ResponseResult(true, message, null);
    }

    public static ResponseResult fail(String message) {
        return new ResponseResult(false, message, null);
    }
}
