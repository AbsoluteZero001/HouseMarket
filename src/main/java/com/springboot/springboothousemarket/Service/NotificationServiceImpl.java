package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Notification;
import com.springboot.springboothousemarket.Mapper.NotificationMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public Notification create(String businessKey, Long userId, String type, String title, String content,
                               String relatedType, Long relatedId) {
        Notification notification = new Notification();
        notification.setBusinessKey(businessKey);
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setReadStatus(0);
        notification.setRetryCount(0);
        notification.setCreateTime(LocalDateTime.now());
        try {
            save(notification);
        } catch (DuplicateKeyException duplicate) {
            // 同一业务事件重复触发：MySQL 唯一键保证不会重复落库，
            // 这里把首次创建的通知返回给调用方，保证上层拿到稳定的 notificationId。
            if (businessKey != null) {
                Notification existing = getOne(new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getBusinessKey, businessKey)
                        .last("LIMIT 1"));
                if (existing != null) {
                    return existing;
                }
            }
            throw duplicate;
        }
        return notification;
    }

    @Override
    public List<Notification> listByUser(Long userId, int limit) {
        return list(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByAsc(Notification::getReadStatus)
                .orderByDesc(Notification::getId)
                .last("LIMIT " + limit));
    }

    @Override
    public long countUnread(Long userId) {
        return lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, 0)
                .count();
    }

    @Override
    public boolean markRead(Long userId, Long id) {
        return lambdaUpdate()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .set(Notification::getReadStatus, 1)
                .update();
    }

    @Override
    public boolean markAllRead(Long userId) {
        return lambdaUpdate()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, 0)
                .set(Notification::getReadStatus, 1)
                .update();
    }

    @Override
    public void markSent(Long notificationId) {
        if (notificationId == null) {
            return;
        }
        lambdaUpdate()
                .eq(Notification::getId, notificationId)
                .set(Notification::getSentTime, LocalDateTime.now())
                .update();
    }

    @Override
    public void markPushFailed(Long notificationId, int retryCount) {
        if (notificationId == null) {
            return;
        }
        lambdaUpdate()
                .eq(Notification::getId, notificationId)
                .set(Notification::getRetryCount, retryCount)
                .update();
    }
}
