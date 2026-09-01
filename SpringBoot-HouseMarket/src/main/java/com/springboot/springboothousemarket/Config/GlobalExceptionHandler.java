package com.springboot.springboothousemarket.Config;

import com.springboot.springboothousemarket.dto.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理：
 * - BusinessException（可预期业务错误）→ 400，消息原文返回；
 * - SecurityException / 未认证 → 401；AccessDeniedException → 403；
 * - 其余未知异常 → 500，统一返回"系统繁忙"，完整堆栈只进服务端日志（防止 SQL/路径等敏感信息外泄）。
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleBusinessException(BusinessException e) {
        return body(400, e.getMessage());
    }

    /**
     * 兼容存量代码中的 RuntimeException 业务错误：消息形如"xxx不能为空"等可读文案，
     * 返回 400 保留原文；无消息或典型框架异常按 500 处理。
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        if (message == null || message.isBlank() || isInternalClassNameMessage(message)) {
            log.error("未处理运行时异常", e);
            return body(500, "系统繁忙，请稍后再试");
        }
        log.warn("业务异常: {}", message);
        return body(400, message);
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public Map<String, Object> handleSecurityException(SecurityException e) {
        return body(401, e.getMessage() == null ? "请先登录" : e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Map<String, Object> handleAccessDenied(AccessDeniedException e) {
        return body(403, "没有权限执行该操作");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleMaxUpload(MaxUploadSizeExceededException e) {
        return body(400, "上传文件大小超出限制");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> handleException(Exception e) {
        log.error("未处理异常", e);
        return body(500, "系统繁忙，请稍后再试");
    }

    private boolean isInternalClassNameMessage(String message) {
        // 常见的裸异常消息（如 NPE 的 null、SQL 异常类名）不外泄
        return message.contains("Exception") || message.contains("###") || message.contains("SQL")
                || message.contains("jdbc") || message.trim().equalsIgnoreCase("null");
    }

    private Map<String, Object> body(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", code < 500);
        response.put("message", message);
        response.put("code", code);
        return response;
    }
}
