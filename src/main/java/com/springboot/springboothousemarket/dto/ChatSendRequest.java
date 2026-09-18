package com.springboot.springboothousemarket.dto;

import lombok.Data;

/**
 * WebSocket 聊天发送负载（客户端 → /app/chat.send）。
 * 发送者身份取自 STOMP 会话认证，不信任客户端字段。
 */
@Data
public class ChatSendRequest {
    private Long toUserId;
    private Long houseId;
    private String content;
}
