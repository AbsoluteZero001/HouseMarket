package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Mapper.NotificationOutboxMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知 Outbox：仅承担 WebSocket 实时推送的可靠投递。
 * 用户可见的通知以 notification 表为准（见 NotificationService）。
 */
@Service
public class NotificationOutboxServiceImpl extends ServiceImpl<NotificationOutboxMapper, NotificationOutbox>
        implements NotificationOutboxService {

    private static final int MAX_RETRY = 3;

    @Override
    @Transactional
    public void enqueue(String businessKey, String businessType, Long appointmentId, String eventType,
                        String payload, Long targetUserId, Long notificationId) {
        NotificationOutbox outbox = new NotificationOutbox();
        outbox.setBusinessKey(businessKey);
        outbox.setBusinessType(businessType);
        outbox.setAppointmentId(appointmentId);
        outbox.setNotificationId(notificationId);
        outbox.setEventType(eventType);
        outbox.setPayload(payload);
        outbox.setTargetUserId(targetUserId);
        outbox.setStatus("pending");
        outbox.setRetryCount(0);
        outbox.setCreateTime(LocalDateTime.now());
        try {
            this.save(outbox);
        } catch (DuplicateKeyException duplicate) {
            // 同一条业务事件已入队，幂等跳过
        }
    }

    @Override
    public List<NotificationOutbox> pollPending(int limit) {
        return this.list(new LambdaQueryWrapper<NotificationOutbox>()
                .eq(NotificationOutbox::getStatus, "pending")
                .orderByAsc(NotificationOutbox::getId)
                .last("LIMIT " + limit));
    }

    @Override
    public boolean claim(Long id) {
        // claim_time 记录「本次领取时刻」：
        // 之前用 create_time 判定僵死，语义是错的 —— 一条刚创建就被领取的记录
        // 会因为 create_time 很旧而被误判为僵死并被反复重置。
        return this.lambdaUpdate()
                .eq(NotificationOutbox::getId, id)
                .eq(NotificationOutbox::getStatus, "pending")
                .set(NotificationOutbox::getStatus, "processing")
                .set(NotificationOutbox::getClaimTime, LocalDateTime.now())
                .update();
    }

    @Override
    public boolean markSent(Long id) {
        return this.lambdaUpdate()
                .eq(NotificationOutbox::getId, id)
                .set(NotificationOutbox::getStatus, "sent")
                .set(NotificationOutbox::getSendTime, LocalDateTime.now())
                .update();
    }

    @Override
    public boolean markFailed(Long id, int retryCount) {
        return this.lambdaUpdate()
                .eq(NotificationOutbox::getId, id)
                .set(NotificationOutbox::getStatus, retryCount >= MAX_RETRY ? "failed" : "pending")
                .set(NotificationOutbox::getRetryCount, retryCount)
                .update();
    }

    @Override
    public boolean deleteByAppointmentId(Long appointmentId) {
        return this.remove(new LambdaQueryWrapper<NotificationOutbox>()
                .eq(NotificationOutbox::getAppointmentId, appointmentId));
    }

    @Override
    public int archiveOlderThan(LocalDateTime cutoff, int batchSize) {
        if (batchSize <= 0) {
            return 0;
        }
        // 只归档「已投递完成」的记录：pending/processing 还在队列里，删掉等于丢消息
        List<NotificationOutbox> rows = list(new LambdaQueryWrapper<NotificationOutbox>()
                .select(NotificationOutbox::getId)
                .in(NotificationOutbox::getStatus, List.of("sent", "failed"))
                .le(NotificationOutbox::getCreateTime, cutoff)
                .last("LIMIT " + batchSize));
        if (rows.isEmpty()) {
            return 0;
        }
        List<Long> ids = rows.stream().map(NotificationOutbox::getId).toList();
        return removeByIds(ids) ? ids.size() : 0;
    }

    @Override
    public void resetStaleProcessing(int minutes) {
        // 以 claim_time（领取时刻）而非 create_time（创建时刻）判定僵死，
        // 否则「创建很久才被领取」的记录一领取就被判超时回收，形成活锁。
        this.lambdaUpdate()
                .eq(NotificationOutbox::getStatus, "processing")
                .isNotNull(NotificationOutbox::getClaimTime)
                .le(NotificationOutbox::getClaimTime, LocalDateTime.now().minusMinutes(minutes))
                .set(NotificationOutbox::getStatus, "pending")
                .set(NotificationOutbox::getClaimTime, null)
                .update();
    }
}
