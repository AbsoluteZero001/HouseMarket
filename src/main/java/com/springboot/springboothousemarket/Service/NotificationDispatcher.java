package com.springboot.springboothousemarket.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springboothousemarket.Entity.Notification;
import com.springboot.springboothousemarket.dto.AppointmentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通知分发门面：业务事件发生时，
 * 1) 在当前业务事务内写入用户通知（notification 表，通知中心数据源）；
 * 2) 写入 Outbox 队列，由 NotificationOutboxProcessor 异步尝试 WebSocket 实时推送。
 * 用户即使错过实时推送，也能在通知中心看到该通知 —— 最终一致。
 */
@Slf4j
@Component
public class NotificationDispatcher {

    private final NotificationService notificationService;
    private final NotificationOutboxService outboxService;
    private final ObjectMapper objectMapper;

    public NotificationDispatcher(NotificationService notificationService,
                                  NotificationOutboxService outboxService,
                                  ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建用户通知并入队 WebSocket 推送。
     *
     * @param businessKey 幂等键（同一业务事件重复调用只会入队一次）
     * @param payload     WebSocket 推送负载（可携带业务上下文字段）
     */
    public void dispatch(String businessKey, String businessType, Long appointmentId,
                         Long userId, String type, String title, String content,
                         String relatedType, Long relatedId, AppointmentMessage payload) {
        if (userId == null) {
            return;
        }
        Notification notification = notificationService.create(userId, type, title, content, relatedType, relatedId);
        if (payload == null) {
            payload = new AppointmentMessage();
        }
        payload.setAppointmentId(appointmentId);
        payload.setStatus(type);
        payload.setTargetUserId(userId);
        payload.setMessage(content);
        payload.setType(type);
        payload.setTitle(title);
        payload.setRelatedType(relatedType);
        payload.setRelatedId(relatedId);
        try {
            String json = objectMapper.writeValueAsString(payload);
            outboxService.enqueue(businessKey, businessType, appointmentId, type, json, userId, notification.getId());
        } catch (Exception e) {
            // 通知落库已成功，仅实时推送入队失败：不影响业务主流程
            log.warn("通知入队失败, businessKey={}, notificationId={}, reason={}", businessKey, notification.getId(), e.getMessage());
        }
    }

    /**
     * 便捷重载：自动构造默认负载。
     */
    public void dispatch(String businessKey, String businessType, Long appointmentId,
                         Long userId, String type, String title, String content,
                         String relatedType, Long relatedId) {
        dispatch(businessKey, businessType, appointmentId, userId, type, title, content,
                relatedType, relatedId, null);
    }
}
