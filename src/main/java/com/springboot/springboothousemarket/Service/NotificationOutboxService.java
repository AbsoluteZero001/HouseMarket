package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;

import java.util.List;

public interface NotificationOutboxService extends IService<NotificationOutbox> {

    void enqueue(String businessKey, String businessType, Long appointmentId, String eventType,
                 String payload, Long targetUserId, Long notificationId);

    List<NotificationOutbox> pollPending(int limit);

    boolean claim(Long id);

    boolean markSent(Long id);

    boolean markFailed(Long id, int retryCount);

    boolean deleteByAppointmentId(Long appointmentId);

    void resetStaleProcessing(int minutes);
}
