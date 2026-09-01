package com.springboot.springboothousemarket.dto;

/**
 * 业务异常：表示可预期的业务规则错误，全局异常处理器会将其消息原文返回给客户端（HTTP 400）。
 * 与其相对，非 BusinessException 的未知异常一律按 500 处理并隐藏内部细节。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
