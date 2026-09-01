package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.ChatService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 聊天 REST 接口：历史消息、会话列表、未读数、已读标记。
 * 所有查询均以当前登录用户为主体，只能看到自己参与的消息。
 */
@Tag(name = "聊天API")
@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    private final ChatService chatService;

    public ChatApiController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/conversations")
    public ResponseResult conversations(@AuthenticationPrincipal Users currentUser) {
        return ResponseResult.ok(null, Map.of("conversations", chatService.conversations(currentUser.getId())));
    }

    @GetMapping("/messages")
    public ResponseResult history(@AuthenticationPrincipal Users currentUser,
                                  @RequestParam Long partnerId,
                                  @RequestParam(required = false) Long houseId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "50") int pageSize) {
        List<?> messages = chatService.history(currentUser.getId(), partnerId, houseId, page, pageSize);
        return ResponseResult.ok(null, Map.of("messages", messages));
    }

    @GetMapping("/unread-count")
    public ResponseResult unreadCount(@AuthenticationPrincipal Users currentUser) {
        return ResponseResult.ok(null, Map.of("unread", chatService.unreadCount(currentUser.getId())));
    }

    @PutMapping("/read/{partnerId}")
    public ResponseResult markRead(@AuthenticationPrincipal Users currentUser,
                                   @PathVariable Long partnerId) {
        int updated = chatService.markRead(currentUser.getId(), partnerId);
        return ResponseResult.ok(null, Map.of("marked", updated));
    }
}
