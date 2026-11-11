package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Notification;

import java.time.LocalDateTime;
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

    /**
     * 分页查询某用户的通知（按未读优先、id 倒序，排序键唯一，翻页结果稳定）。
     */
    List<Notification> listByUserPaged(Long userId, int offset, int size);

    /**
     * 某用户的通知总数（用于分页）。
     */
    long countByUser(Long userId);

    long countUnread(Long userId);

    boolean markRead(Long userId, Long id);

    boolean markAllRead(Long userId);

    void markSent(Long notificationId);

    void markPushFailed(Long notificationId, int retryCount);

    /**
     * 通知归档：删除超过保留期的历史通知，控制单表规模。
     *
     * @param readCutoff   已读通知的归档线（早于该时间的已读通知被删除）
     * @param unreadCutoff 未读通知的归档线（未读保留更久，避免用户还没看的消息被清掉）
     * @param batchSize    单次最多删除的条数，避免一次锁表过久
     * @return 实际删除的条数
     */
    int archiveOlderThan(LocalDateTime readCutoff, LocalDateTime unreadCutoff, int batchSize);
}
