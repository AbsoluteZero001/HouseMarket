package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Notification;
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

import java.util.LinkedHashMap;
import java.util.List;
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

    /**
     * 通知列表（分页）。
     *
     * 之前只有 limit 一个参数，最多返回最新 100 条且无法翻页，
     * 用户看不到更早的通知 —— 这里补上 page/size 分页，并保留 limit 兼容旧调用方。
     *
     * @param page 页码，从 1 开始；不传按第 1 页处理
     * @param size 每页条数，1~100；不传时取 limit（兼容旧参数）
     * @param limit 旧参数：单次最多返回多少条（未传 page/size 时生效）
     */
    @GetMapping
    public ResponseResult getNotifications(@AuthenticationPrincipal Users currentUser,
                                           @RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size,
                                           @RequestParam(defaultValue = "50") int limit) {
        int safeSize = size != null ? Math.min(Math.max(size, 1), 100) : Math.min(Math.max(limit, 1), 100);
        int safePage = page != null ? Math.max(page, 1) : 1;
        int offset = (safePage - 1) * safeSize;
        List<Notification> records = notificationService.listByUserPaged(currentUser.getId(), offset, safeSize);
        long total = notificationService.countByUser(currentUser.getId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("notifications", records);
        data.put("unread", notificationService.countUnread(currentUser.getId()));
        data.put("total", total);
        data.put("page", safePage);
        data.put("size", safeSize);
        data.put("hasMore", offset + records.size() < total);
        return ResponseResult.ok(null, data);
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
