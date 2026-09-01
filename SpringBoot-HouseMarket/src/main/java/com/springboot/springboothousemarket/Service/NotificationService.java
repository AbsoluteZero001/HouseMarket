package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    /**
     * 创建一条用户通知（业务事务内调用，与业务写入同事务）。
     */
    Notification create(Long userId, String type, String title, String content, String relatedType, Long relatedId);

    List<Notification> listByUser(Long userId, int limit);

    long countUnread(Long userId);

    boolean markRead(Long userId, Long id);

    boolean markAllRead(Long userId);

    void markSent(Long notificationId);

    void markPushFailed(Long notificationId, int retryCount);
}
