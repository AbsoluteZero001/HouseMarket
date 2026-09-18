package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.NotificationService;
import com.springboot.springboothousemarket.dto.ResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 通知中心 API：数据源为 notification 表（业务事件发生时落库）。
 * 未读/已读状态由用户主动维护；WebSocket 实时推送只是触达加速。
 */
@Tag(name = "通知中心API")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseResult getNotifications(@AuthenticationPrincipal Users currentUser,
                                           @RequestParam(defaultValue = "50") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return ResponseResult.ok(null, Map.of(
                "notifications", notificationService.listByUser(currentUser.getId(), safeLimit),
                "unread", notificationService.countUnread(currentUser.getId())));
    }

    @GetMapping("/unread-count")
    public ResponseResult unreadCount(@AuthenticationPrincipal Users currentUser) {
        return ResponseResult.ok(null, Map.of("unread", notificationService.countUnread(currentUser.getId())));
    }

    @PutMapping("/{id}/read")
    public ResponseResult markRead(@AuthenticationPrincipal Users currentUser, @PathVariable Long id) {
        boolean ok = notificationService.markRead(currentUser.getId(), id);
        return ok ? ResponseResult.ok("已标记已读") : ResponseResult.fail("通知不存在");
    }

    @PutMapping("/read-all")
    public ResponseResult markAllRead(@AuthenticationPrincipal Users currentUser) {
        notificationService.markAllRead(currentUser.getId());
        return ResponseResult.ok("已全部标记已读");
    }
}
