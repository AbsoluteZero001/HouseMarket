package com.springboot.springboothousemarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult {
    /**
     * 业务状态码，与 HTTP 状态码保持一致：成功 200，失败 400。
     * 之前本类没有 code 字段，而全局异常处理器返回的却是带 code 的结构，
     * 两套响应体不一致，前端无法用同一个字段判断成败。
     */
    private int code;

    /** 业务是否成功：仅当 code 为 2xx 时为 true。 */
    private boolean success;

    private String message;

    private Object data;

    public static ResponseResult ok(String message, Object data) {
        return new ResponseResult(200, true, message, data);
    }

    public static ResponseResult ok(String message) {
        return new ResponseResult(200, true, message, null);
    }

    public static ResponseResult fail(String message) {
        return new ResponseResult(400, false, message, null);
    }
}
