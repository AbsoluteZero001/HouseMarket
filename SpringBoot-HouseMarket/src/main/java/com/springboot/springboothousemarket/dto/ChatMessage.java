package com.springboot.springboothousemarket.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long houseId;
    private Long fromUserId;
    private Long toUserId;
    private String fromName;
    private String content;
    private LocalDateTime timestamp;
    private String type;
}
