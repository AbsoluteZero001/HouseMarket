package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.NotificationOutboxService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "通知中心API")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationOutboxService outboxService;

    public NotificationController(NotificationOutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @GetMapping
    public ResponseResult getNotifications(@AuthenticationPrincipal Users currentUser,
                                           @RequestParam(defaultValue = "50") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseResult.ok(null, Map.of(
                "notifications", outboxService.getNotificationsByUserId(currentUser.getId(), safeLimit)));
    }
}
