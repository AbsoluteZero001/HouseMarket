package com.springboot.springboothousemarket.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 通知归档任务：定期清理超过保留期的历史通知与已投递完成的 Outbox 记录。
 *
 * 背景：notification / notification_outbox 是只增不改的流水表，
 * business_key 唯一索引与通知中心查询都会随数据量增长而劣化。
 * 这里用「已读短期保留、未读长期保留」的两级保留策略，
 * 既控制单表规模，又不会把用户还没看到的消息清掉。
 */
@Slf4j
@Component
public class NotificationArchiveJob {

    private final NotificationService notificationService;
    private final NotificationOutboxService outboxService;

    @Value("${app.notification.archive-enabled:true}")
    private boolean enabled;

    @Value("${app.notification.archive-days:90}")
    private int archiveDays;

    @Value("${app.notification.archive-unread-days:365}")
    private int unreadArchiveDays;

    @Value("${app.notification.archive-batch-size:1000}")
    private int batchSize;

    public NotificationArchiveJob(NotificationService notificationService,
                                   NotificationOutboxService outboxService) {
        this.notificationService = notificationService;
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelayString = "${app.notification.archive-interval-ms:21600000}")
    public void archiveExpiredNotifications() {
        if (!enabled) {
            return;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime readCutoff = now.minusDays(Math.max(archiveDays, 1));
            LocalDateTime unreadCutoff = now.minusDays(Math.max(unreadArchiveDays, 1));
            // 先清 Outbox 再清通知：Outbox 里还引用着 notification_id
            int outboxRemoved = outboxService.archiveOlderThan(readCutoff, batchSize);
            int notificationRemoved = notificationService.archiveOlderThan(readCutoff, unreadCutoff, batchSize);
            if (outboxRemoved + notificationRemoved > 0) {
                log.info("通知归档完成: notification={} 条, outbox={} 条", notificationRemoved, outboxRemoved);
            }
        } catch (Exception e) {
            // 归档失败不影响业务，下一轮定时任务会继续尝试
            log.warn("通知归档任务失败, reason={}", e.getMessage());
        }
    }
}
