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
    public List<Notification> listByUserPaged(Long userId, int offset, int size) {
        int safeSize = Math.min(Math.max(size, 1), 200);
        int safeOffset = Math.max(offset, 0);
        // 排序键 (read_status, id) 唯一且稳定，翻页不会出现重复/漏项
        return list(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByAsc(Notification::getReadStatus)
                .orderByDesc(Notification::getId)
                .last("LIMIT " + safeSize + " OFFSET " + safeOffset));
    }

    @Override
    public long countByUser(Long userId) {
        return lambdaQuery()
                .eq(Notification::getUserId, userId)
                .count();
    }

    @Override
    public int archiveOlderThan(LocalDateTime readCutoff, LocalDateTime unreadCutoff, int batchSize) {
        if (batchSize <= 0) {
            return 0;
        }
        int removed = 0;
        // 已读通知：超过常规保留期即归档
        removed += deleteBatch(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReadStatus, 1)
                .le(Notification::getCreateTime, readCutoff), batchSize);
        // 未读通知：保留更久，避免用户还没来得及看的消息被清掉
        removed += deleteBatch(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReadStatus, 0)
                .le(Notification::getCreateTime, unreadCutoff), batchSize);
        return removed;
    }

    /**
     * 分批删除：先按条件取主键，再按主键删除，避免一条 DELETE 扫全表并长时间持锁。
     */
    private int deleteBatch(LambdaQueryWrapper<Notification> wrapper, int batchSize) {
        List<Notification> rows = list(wrapper.select(Notification::getId).last("LIMIT " + batchSize));
        if (rows.isEmpty()) {
            return 0;
        }
        List<Long> ids = rows.stream().map(Notification::getId).toList();
        return removeByIds(ids) ? ids.size() : 0;
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
