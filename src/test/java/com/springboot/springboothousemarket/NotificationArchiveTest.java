package com.springboot.springboothousemarket;

import com.springboot.springboothousemarket.Entity.Notification;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Service.NotificationOutboxService;
import com.springboot.springboothousemarket.Service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 通知归档与分页。
 *
 * <p>背景：notification / notification_outbox 是只增不改的流水表，
 * 唯一索引与通知中心查询都会随数据量增长而劣化；通知列表接口原来只有
 * limit 参数（最多 100 条且无法翻页），用户看不到更早的通知。
 * 这里验证两级保留策略（已读短期 / 未读长期）、Outbox 终态清理，以及分页的一致性。
 */
@SpringBootTest
@Transactional
class NotificationArchiveTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationOutboxService outboxService;

    /**
     * 归档策略：已读超 90 天被清掉；未读超 90 天仍保留（用户还没看）；
     * 未读超 365 天才清掉；新通知不受影响。
     */
    @Test
    void archiveRemovesOldReadAndAncientUnreadOnly() {
        Long userId = 7L;
        Notification oldRead = save(userId, 1, LocalDateTime.now().minusDays(100));
        Notification oldUnread = save(userId, 0, LocalDateTime.now().minusDays(100));
        Notification ancientUnread = save(userId, 0, LocalDateTime.now().minusDays(400));
        Notification fresh = save(userId, 0, LocalDateTime.now());

        int removed = notificationService.archiveOlderThan(
                LocalDateTime.now().minusDays(90),
                LocalDateTime.now().minusDays(365),
                1000);
        assertEquals(2, removed, "只应删除「已读超期」与「未读超 365 天」两类");

        assertNull(notificationService.getById(oldRead.getId()), "已读超期通知应被归档");
        assertNull(notificationService.getById(ancientUnread.getId()), "未读超 365 天通知应被归档");
        assertNotNull(notificationService.getById(oldUnread.getId()), "未读超 90 天但未到 365 天，必须保留");
        assertNotNull(notificationService.getById(fresh.getId()), "新通知必须保留");
    }

    /**
     * Outbox 只能清理终态记录：pending/processing 还在投递队列里，删掉等于丢消息。
     */
    @Test
    void archiveRemovesOnlyTerminalOutboxRecords() {
        NotificationOutbox sent = outbox("sent");
        NotificationOutbox failed = outbox("failed");
        NotificationOutbox pending = outbox("pending");
        NotificationOutbox processing = outbox("processing");
        outboxService.save(sent);
        outboxService.save(failed);
        outboxService.save(pending);
        outboxService.save(processing);

        int removed = outboxService.archiveOlderThan(LocalDateTime.now().minusDays(90), 1000);
        assertEquals(2, removed, "只应删除 sent / failed 两种终态记录");

        assertNull(outboxService.getById(sent.getId()));
        assertNull(outboxService.getById(failed.getId()));
        assertNotNull(outboxService.getById(pending.getId()), "pending 仍在队列中，不能删");
        assertNotNull(outboxService.getById(processing.getId()), "processing 正在投递，不能删");
    }

    /**
     * 分页与整页查询必须给出完全一致的顺序与内容 ——
     * 否则前端「加载更多」会出现重复或漏项。
     */
    @Test
    void pagedListingMatchesFullListing() {
        Long userId = 5L;
        for (int i = 0; i < 7; i++) {
            save(userId, i % 2, LocalDateTime.now().minusMinutes(i));
        }

        List<Notification> all = notificationService.listByUser(userId, 1000);
        List<Notification> paged = notificationService.listByUserPaged(userId, 0, 1000);
        assertEquals(all.size(), paged.size(), "分页总数与整页查询必须一致");
        for (int i = 0; i < all.size(); i++) {
            assertEquals(all.get(i).getId(), paged.get(i).getId(), "第 " + i + " 条顺序不一致");
        }
        assertEquals(all.size(), notificationService.countByUser(userId), "total 必须与实际条数一致");

        // 按 3 条一页切片，结果必须与整页逐段一致（无重复、无漏项）
        int chunk = 3;
        for (int offset = 0; offset < all.size(); offset += chunk) {
            List<Notification> page = notificationService.listByUserPaged(userId, offset, chunk);
            assertEquals(Math.min(chunk, all.size() - offset), page.size());
            for (int i = 0; i < page.size(); i++) {
                assertEquals(all.get(offset + i).getId(), page.get(i).getId());
            }
        }

        // 排序规则：未读在前（read_status 升序），同状态内 id 倒序
        for (int i = 1; i < all.size(); i++) {
            Notification prev = all.get(i - 1);
            Notification cur = all.get(i);
            boolean ordered = prev.getReadStatus() < cur.getReadStatus()
                    || (prev.getReadStatus().equals(cur.getReadStatus()) && prev.getId() > cur.getId());
            assertTrue(ordered, "排序必须稳定：未读优先 + id 倒序");
        }
    }

    private Notification save(Long userId, int readStatus, LocalDateTime createTime) {
        Notification notification = new Notification();
        notification.setBusinessKey("EVT#TEST#archive#" + UUID.randomUUID());
        notification.setUserId(userId);
        notification.setType("TEST");
        notification.setTitle("标题");
        notification.setContent("内容");
        notification.setRelatedType("TEST");
        notification.setRelatedId(1L);
        notification.setReadStatus(readStatus);
        notification.setRetryCount(0);
        notification.setCreateTime(createTime);
        notificationService.save(notification);
        assertNotNull(notification.getId());
        return notification;
    }

    private NotificationOutbox outbox(String status) {
        NotificationOutbox outbox = new NotificationOutbox();
        outbox.setBusinessKey("EVT#TEST#archive-outbox#" + UUID.randomUUID());
        outbox.setBusinessType("TEST");
        outbox.setEventType("TEST_EVENT");
        outbox.setPayload("{}");
        outbox.setTargetUserId(7L);
        outbox.setStatus(status);
        outbox.setRetryCount(0);
        outbox.setCreateTime(LocalDateTime.now().minusDays(100));
        return outbox;
    }
}
