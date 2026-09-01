package com.springboot.springboothousemarket.dto;

import lombok.Data;

/**
 * 预约消息模型
 * 用于WebSocket实时通知预约状态变更
 */
@Data
public class AppointmentMessage {
    private Long appointmentId;
    private String status; // 事件类型，如 APPROVED / REJECTED / APPOINTMENT_CREATED / LANDLORD_APPROVED
    private Long tenantId;
    private Long landlordId;
    private Long targetUserId;
    private String message; // 可选的消息内容
    private String type;    // 通知类型
    private String title;   // 通知标题
    private String relatedType; // 关联业务类型
    private Long relatedId;     // 关联业务ID
}
