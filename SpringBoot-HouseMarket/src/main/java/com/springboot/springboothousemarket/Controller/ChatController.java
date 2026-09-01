package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Service.ChatService;
import com.springboot.springboothousemarket.dto.ChatSendRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * 聊天 WebSocket 入口：消息一律先经 ChatService 落库，再做实时推送。
 * 身份来自 STOMP 会话认证（WebSocketAuthInterceptor 注入，Principal name = 用户ID），
 * 客户端上报的 fromUserId 一律忽略，防止伪造发送者。
 */
@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void send(@Payload ChatSendRequest payload, Principal principal) {
        Long senderId = currentUserId(principal);
        if (senderId == null) {
            return;
        }
        chatService.send(senderId, payload.getToUserId(), payload.getHouseId(), payload.getContent());
    }

    private Long currentUserId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof String userId) {
            try {
                return Long.valueOf(userId);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
