package com.springboot.springboothousemarket.Config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理类
 * 用于统一处理所有控制器抛出的异常，返回统一的JSON格式响应
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有异常
     *
     * @param e 异常对象
     * @return 统一格式的JSON响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> handleException(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        response.put("code", 500);
        // 可以根据需要添加更多字段，如错误码、时间戳等
        return response;
    }

    /**
     * 处理SecurityException异常
     *
     * @param e 异常对象
     * @return 统一格式的JSON响应
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public Map<String, Object> handleSecurityException(SecurityException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        response.put("code", 401);
        return response;
    }

    /**
     * 处理RuntimeException异常
     *
     * @param e 异常对象
     * @return 统一格式的JSON响应
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleRuntimeException(RuntimeException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        response.put("code", 400);
        return response;
    }
}