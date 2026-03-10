package com.springboot.springboothousemarket.dto;

import lombok.Data;

/**
 * 预约消息模型
 * 用于WebSocket实时通知预约状态变更
 */
@Data
public class AppointmentMessage {
    private Long appointmentId;
    private String status; // APPROVED / REJECTED / PENDING
    private Long tenantId;
    private Long landlordId;
    private String message; // 可选的消息内容
}