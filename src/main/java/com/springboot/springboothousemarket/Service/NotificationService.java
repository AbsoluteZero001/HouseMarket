package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {

    /**
     * 创建一条用户通知（业务事务内调用，与业务写入同事务）。
     *
     * @param businessKey 业务幂等键（事件+接收用户维度）。相同键重复调用不会重复落库，
     *                    返回首次创建的那条记录；为 null 表示不参与去重。
     */
    Notification create(String businessKey, Long userId, String type, String title, String content,
                        String relatedType, Long relatedId);

    List<Notification> listByUser(Long userId, int limit);

    long countUnread(Long userId);

    boolean markRead(Long userId, Long id);

    boolean markAllRead(Long userId);

    void markSent(Long notificationId);

    void markPushFailed(Long notificationId, int retryCount);
}
