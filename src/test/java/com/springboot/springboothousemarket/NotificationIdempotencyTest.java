package com.springboot.springboothousemarket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springboot.springboothousemarket.Entity.Notification;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Service.NotificationDispatcher;
import com.springboot.springboothousemarket.Service.NotificationOutboxService;
import com.springboot.springboothousemarket.Service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 通知模块的幂等性与 Outbox 回收语义。
 */
@SpringBootTest
@Transactional
class NotificationIdempotencyTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationOutboxService outboxService;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    /**
     * 回归：同一 businessKey 重复创建通知只能落库一条，并返回首次那条记录。
     */
    @Test
    void sameBusinessKeyCreatesOnlyOneNotification() {
        String businessKey = "EVT#TEST#idempotent";
        Notification first = notificationService.create(businessKey, 7L, "TEST", "标题", "内容", "APPOINTMENT", 1L);
        Notification second = notificationService.create(businessKey, 7L, "TEST", "标题", "内容", "APPOINTMENT", 1L);

        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertEquals(first.getId(), second.getId());

        long rows = notificationService.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getBusinessKey, businessKey));
        assertEquals(1L, rows);
    }

    /**
     * 回归：同一个业务事件要同时通知房东与租客。
     * 幂等键必须带上「接收用户」维度，否则第二位收件人会被唯一索引挡掉，
     * 结果是「其中一方永远收不到通知 / 收不到实时推送」。
     */
    @Test
    void dispatchNotifiesBothPartiesOfTheSameEvent() {
        String businessKey = "EVT#TEST#both";
        notificationDispatcher.dispatch(businessKey, "APPOINTMENT", null, 5L,
                "APPOINTMENT_CREATED", "标题", "内容", "APPOINTMENT", 100L);
        notificationDispatcher.dispatch(businessKey, "APPOINTMENT", null, 6L,
                "APPOINTMENT_CREATED", "标题", "内容", "APPOINTMENT", 100L);

        assertEquals(1L, notificationService.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getBusinessKey, businessKey + "#5")));
        assertEquals(1L, notificationService.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getBusinessKey, businessKey + "#6")));

        assertEquals(2L, outboxService.count(new LambdaQueryWrapper<NotificationOutbox>()
                .in(NotificationOutbox::getBusinessKey, businessKey + "#5", businessKey + "#6")));
    }

    /**
     * 回归：Outbox 的僵死回收必须以「领取时刻」为准。
     * 旧实现按 create_time 判定，会把「刚创建就被领取」的记录立刻重置成 pending，形成活锁。
     */
    @Test
    void claimRecordsClaimTimeAndStaleResetUsesIt() {
        NotificationOutbox outbox = new NotificationOutbox();
        outbox.setBusinessKey("EVT#TEST#claim");
        outbox.setBusinessType("APPOINTMENT");
        outbox.setEventType("APPOINTMENT_CREATED");
        outbox.setPayload("{}");
        outbox.setTargetUserId(7L);
        outbox.setStatus("pending");
        outbox.setRetryCount(0);
        outbox.setCreateTime(LocalDateTime.now());
        outboxService.save(outbox);
        assertNotNull(outbox.getId());

        assertTrue(outboxService.claim(outbox.getId()));
        NotificationOutbox claimed = outboxService.getById(outbox.getId());
        assertEquals("processing", claimed.getStatus());
        assertNotNull(claimed.getClaimTime(), "领取时必须记录 claim_time");

        // 刚领取的记录不能被回收
        outboxService.resetStaleProcessing(30);
        assertEquals("processing", outboxService.getById(outbox.getId()).getStatus());

        // 领取时刻已久远 → 回收为 pending 并清空 claim_time
        NotificationOutbox stale = new NotificationOutbox();
        stale.setId(outbox.getId());
        stale.setClaimTime(LocalDateTime.now().minusHours(2));
        outboxService.updateById(stale);

        outboxService.resetStaleProcessing(30);
        NotificationOutbox recovered = outboxService.getById(outbox.getId());
        assertEquals("pending", recovered.getStatus());
        assertNull(recovered.getClaimTime());
    }
}