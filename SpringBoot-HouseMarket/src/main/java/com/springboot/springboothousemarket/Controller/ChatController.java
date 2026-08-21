package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UsersService usersService;

    public ChatController(SimpMessagingTemplate messagingTemplate, UsersService usersService) {
        this.messagingTemplate = messagingTemplate;
        this.usersService = usersService;
    }

    @MessageMapping("/chat.send")
    public void send(ChatMessage message, Principal principal) {
        Users sender = currentUser(principal);
        if (sender == null || message.getToUserId() == null) {
            return;
        }
        message.setFromUserId(sender.getId());
        message.setFromName(sender.getNickname() != null && !sender.getNickname().isBlank()
                ? sender.getNickname()
                : sender.getRealName() != null && !sender.getRealName().isBlank()
                  ? sender.getRealName()
                  : sender.getUsername());
        message.setTimestamp(LocalDateTime.now());
        message.setType("CHAT");

        messagingTemplate.convertAndSend("/queue/chat/" + message.getToUserId(), message);
        messagingTemplate.convertAndSend("/queue/chat/" + sender.getId(), message);
    }

    private Users currentUser(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof Users users) {
            return users;
        }
        if (principal == null) {
            return null;
        }
        return usersService.getUserByUsername(principal.getName());
    }
}
