package com.springboot.springboothousemarket.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.dto.AppointmentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 事务 Outbox 异步投递器：轮询待发送通知，通过 WebSocket 推送给目标用户。
 * 使用 claim 条件更新保证多实例下每条消息只被一个消费者处理。
 */
@Component
public class NotificationOutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationOutboxProcessor.class);

    private final NotificationOutboxService outboxService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationOutboxProcessor(NotificationOutboxService outboxService,
                                       SimpMessagingTemplate messagingTemplate,
                                       ObjectMapper objectMapper) {
        this.outboxService = outboxService;
        this.messagingTemplate = messagingTemplate;
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
                if (message.getTargetUserId() != null) {
                    messagingTemplate.convertAndSendToUser(
                            message.getTargetUserId().toString(),
                            "/queue/appointment",
                            message);
                }
                outboxService.markSent(item.getId());
            } catch (Exception e) {
                int retry = (item.getRetryCount() == null ? 0 : item.getRetryCount()) + 1;
                outboxService.markFailed(item.getId(), retry);
                log.warn("通知投递失败, outboxId={}, retry={}, reason={}",
                        item.getId(), retry, e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox.reset-interval:60000}")
    public void resetStaleProcessing() {
        outboxService.resetStaleProcessing(5);
    }
}
