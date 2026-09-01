package com.springboot.springboothousemarket.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.dto.AppointmentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 事务 Outbox 异步投递器：轮询待发送通知，通过 WebSocket 推送给目标用户。
 *
 * 投递语义（与旧版"发完即 markSent"不同）：
 * - 只有目标用户确实存在 WebSocket 会话且推送执行成功，才标记 sent；
 * - 用户离线时保持 pending 重试若干次后转 failed —— 通知不会丢，
 *   用户上线后可在通知中心（notification 表）看到全部历史通知；
 * - claim 条件更新保证多实例下每条消息只被一个消费者处理，避免重复发送。
 */
@Slf4j
@Component
public class NotificationOutboxProcessor {

    private final NotificationOutboxService outboxService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;
    private final ObjectMapper objectMapper;

    public NotificationOutboxProcessor(NotificationOutboxService outboxService,
                                       NotificationService notificationService,
                                       SimpMessagingTemplate messagingTemplate,
                                       SimpUserRegistry userRegistry,
                                       ObjectMapper objectMapper) {
        this.outboxService = outboxService;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval:2000}")
    public void processPendingNotifications() {
        List<NotificationOutbox> batch = outboxService.pollPending(50);
        for (NotificationOutbox item : batch) {
            if (!outboxService.claim(item.getId())) {
                continue;
            }
            try {
                AppointmentMessage message = objectMapper.readValue(item.getPayload(), AppointmentMessage.class);
                boolean delivered = deliver(message);
                if (delivered) {
                    outboxService.markSent(item.getId());
                    notificationService.markSent(item.getNotificationId());
                } else {
                    int retry = (item.getRetryCount() == null ? 0 : item.getRetryCount()) + 1;
                    outboxService.markFailed(item.getId(), retry);
                    notificationService.markPushFailed(item.getNotificationId(), retry);
                    if (log.isDebugEnabled()) {
                        log.debug("用户不在线，通知等待重试, outboxId={}, retry={}", item.getId(), retry);
                    }
                }
            } catch (Exception e) {
                int retry = (item.getRetryCount() == null ? 0 : item.getRetryCount()) + 1;
                outboxService.markFailed(item.getId(), retry);
                log.warn("通知投递失败, outboxId={}, retry={}, reason={}",
                        item.getId(), retry, e.getMessage());
            }
        }
    }

    /**
     * 尝试向目标用户推送。返回是否真正送达（用户在线且推送未抛异常）。
     */
    private boolean deliver(AppointmentMessage message) {
        if (message == null || message.getTargetUserId() == null) {
            // 无目标用户的广播类负载：无需定向推送，直接视为完成
            return true;
        }
        String targetUserId = message.getTargetUserId().toString();
        boolean online = userRegistry.getUser(targetUserId) != null
                && userRegistry.getUser(targetUserId).hasSessions();
        if (!online) {
            return false;
        }
        messagingTemplate.convertAndSendToUser(targetUserId, "/queue/appointment", message);
        return true;
    }

    @Scheduled(fixedDelayString = "${app.outbox.reset-interval:60000}")
    public void resetStaleProcessing() {
        outboxService.resetStaleProcessing(5);
    }
}
