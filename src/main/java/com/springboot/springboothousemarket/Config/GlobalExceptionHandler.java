package com.springboot.springboothousemarket.Config;

import com.springboot.springboothousemarket.dto.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理：
 * <ul>
 *   <li>BusinessException（可预期业务错误）→ 400，消息原文返回；</li>
 *   <li>参数绑定/反序列化错误 → 400，统一脱敏文案（不外泄内部类型与字段细节）；</li>
 *   <li>SecurityException → 401；AccessDeniedException → 403；</li>
 *   <li>其余未知异常 → 500，统一返回"系统繁忙"，完整堆栈只进服务端日志（防止 SQL/路径等敏感信息外泄）。</li>
 * </ul>
 * <p>HTTP 状态码与响应体中的 {@code code} 始终保持一致：不再出现
 * 「响应体 code=500 但 HTTP 状态为 400」这类错配；{@code success} 仅在 2xx 时为 true，
 * 避免前端把 4xx 错误响应误判为成功。
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(400, e.getMessage()));
    }

    /**
     * 请求参数绑定 / 反序列化类错误：返回 400 与脱敏文案。
     * 这类异常原本会落到 RuntimeException 分支，把 Spring 内部消息
     * （如 "Failed to convert value of type ... to required type ..."）直接外泄给客户端。
     */
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            BindException.class,
            MissingServletRequestParameterException.class,
            ServletRequestBindingException.class})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(Exception e) {
        log.warn("请求参数不合法: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(400, "请求参数格式不正确"));
    }

    /**
     * 兼容存量代码中的 RuntimeException 业务错误：消息形如"xxx不能为空"等可读文案，
     * 返回 400 保留原文；编程错误与框架异常按 500 处理并隐藏内部细节。
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        if (isProgrammingError(e)) {
            log.error("未处理运行时异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(body(500, "系统繁忙，请稍后再试"));
        }
        String message = e.getMessage();
        if (message == null || message.isBlank() || isInternalClassNameMessage(message)) {
            log.error("未处理运行时异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(body(500, "系统繁忙，请稍后再试"));
        }
        log.warn("业务异常: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(400, message));
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(body(401, e.getMessage() == null ? "请先登录" : e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(403, "没有权限执行该操作"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(400, "上传文件大小超出限制"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("未处理异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(500, "系统繁忙，请稍后再试"));
    }

    /**
     * 编程错误 / 数据完整性错误：这类异常的原始消息会泄露内部实现（NPE 堆栈、SQL 片段、
     * 约束名等），一律按 500 处理并返回统一文案。
     */
    private boolean isProgrammingError(Throwable e) {
        return e instanceof NullPointerException
                || e instanceof DataIntegrityViolationException
                || e instanceof IllegalStateException
                || e instanceof ArrayIndexOutOfBoundsException
                || e instanceof ClassCastException
                || e instanceof NumberFormatException;
    }

    private boolean isInternalClassNameMessage(String message) {
        // 常见的裸异常消息（如 NPE 的 null、SQL 异常类名）不外泄
        return message.contains("Exception") || message.contains("###") || message.contains("SQL")
                || message.contains("jdbc") || message.trim().equalsIgnoreCase("null");
    }

    /**
     * 统一响应体：{@code success} 仅在 HTTP 2xx 时为 true，
     * 保证「业务失败」永远不会带上 success=true 的误导性标志。
     */
    private Map<String, Object> body(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", code >= 200 && code < 300);
        response.put("message", message);
        response.put("code", code);
        return response;
    }
}