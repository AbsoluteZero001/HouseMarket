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

@Service
public class NotificationOutboxServiceImpl extends ServiceImpl<NotificationOutboxMapper, NotificationOutbox>
        implements NotificationOutboxService {

    @Override
    @Transactional
    public void enqueue(String businessKey, String businessType, Long appointmentId, String eventType,
                        String payload, Long targetUserId) {
        NotificationOutbox outbox = new NotificationOutbox();
        outbox.setBusinessKey(businessKey);
        outbox.setBusinessType(businessType);
        outbox.setAppointmentId(appointmentId);
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
        return this.lambdaUpdate()
                .eq(NotificationOutbox::getId, id)
                .eq(NotificationOutbox::getStatus, "pending")
                .set(NotificationOutbox::getStatus, "processing")
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
                .set(NotificationOutbox::getStatus, retryCount >= 3 ? "failed" : "pending")
                .set(NotificationOutbox::getRetryCount, retryCount)
                .update();
    }

    @Override
    public boolean deleteByAppointmentId(Long appointmentId) {
        return this.remove(new LambdaQueryWrapper<NotificationOutbox>()
                .eq(NotificationOutbox::getAppointmentId, appointmentId));
    }

    @Override
    public void resetStaleProcessing(int minutes) {
        this.lambdaUpdate()
                .eq(NotificationOutbox::getStatus, "processing")
                .le(NotificationOutbox::getCreateTime, LocalDateTime.now().minusMinutes(minutes))
                .set(NotificationOutbox::getStatus, "pending")
                .update();
    }

    @Override
    public List<NotificationOutbox> getNotificationsByUserId(Long userId, int limit) {
        return this.list(new LambdaQueryWrapper<NotificationOutbox>()
                .eq(NotificationOutbox::getTargetUserId, userId)
                .eq(NotificationOutbox::getStatus, "sent")
                .orderByDesc(NotificationOutbox::getId)
                .last("LIMIT " + limit));
    }
}
