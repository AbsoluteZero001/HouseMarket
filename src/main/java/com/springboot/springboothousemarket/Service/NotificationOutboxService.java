package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;

import java.time.LocalDateTime;
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

    /**
     * Outbox 归档：只清理「已投递完成（sent/failed）」且超过保留期的记录。
     * pending/processing 仍在投递队列中，删掉等于丢消息。
     *
     * @return 实际删除的条数
     */
    int archiveOlderThan(LocalDateTime cutoff, int batchSize);
}
