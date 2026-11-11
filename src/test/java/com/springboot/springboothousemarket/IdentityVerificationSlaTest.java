package com.springboot.springboothousemarket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springboot.springboothousemarket.Entity.IdentityVerification;
import com.springboot.springboothousemarket.Entity.Notification;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.IdentityVerificationService;
import com.springboot.springboothousemarket.Service.NotificationService;
import com.springboot.springboothousemarket.Service.UsersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实名认证审核 SLA：超期自动拒绝。
 *
 * <p>背景：实名认证是纯人工审核流程，原来没有任何时限约束 ——
 * 管理员不处理时，申请会无限期停在 pending，用户既拿不到结果，
 * 也不知道还要等多久，业务闭环是断的。这里验证：
 * <ol>
 *   <li>超过 SLA（默认 72 小时）仍 pending 的申请会被自动置为 rejected 并通知申请人；</li>
 *   <li>未超期的申请不受影响；</li>
 *   <li>重新提交会刷新计时起点（新的审核轮次）。</li>
 * </ol>
 */
@SpringBootTest
@Transactional
class IdentityVerificationSlaTest {

    @Autowired
    private IdentityVerificationService identityVerificationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UsersService usersService;

    /**
     * 超期申请自动拒绝 + 生成超时通知。
     */
    @Test
    void overduePendingVerificationIsRejectedAndNotified() {
        Long userId = 6L;
        clear(userId);
        IdentityVerification overdue = pending(userId, "tenant2", "110101199001011230",
                LocalDateTime.now().minusHours(100));
        identityVerificationService.save(overdue);

        int rejected = identityVerificationService.expireOverdue(72);
        assertEquals(1, rejected, "超过 72 小时仍未审核的申请必须被自动拒绝");

        IdentityVerification after = identityVerificationService.getById(overdue.getId());
        assertNotNull(after);
        assertEquals("rejected", after.getStatus());
        assertNotNull(after.getReviewTime(), "自动拒绝必须留下审核时间");
        assertTrue(after.getReviewNote() != null && after.getReviewNote().contains("自动拒绝"),
                "拒绝原因必须说明是系统超时自动拒绝，实际: " + after.getReviewNote());

        long expiredNotifications = notificationService.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getType, "IDENTITY_EXPIRED"));
        assertEquals(1L, expiredNotifications, "申请人必须收到超时通知，否则不知道申请已被拒绝");
    }

    /**
     * 未超期的申请不能被误伤。
     */
    @Test
    void freshPendingVerificationIsNotExpired() {
        Long userId = 7L;
        clear(userId);
        IdentityVerification fresh = pending(userId, "tenant3", "110101199001011231",
                LocalDateTime.now().minusHours(1));
        identityVerificationService.save(fresh);

        assertEquals(0, identityVerificationService.expireOverdue(72));
        assertEquals("pending", identityVerificationService.getById(fresh.getId()).getStatus());
    }

    /**
     * 重新提交 = 新的一轮审核，SLA 计时重新开始。
     * （否则用户重新提交后仍然会立刻被判超时，等于不让用户重试。）
     */
    @Test
    void resubmittingRefreshesTheSlaClock() {
        Long userId = 8L;
        clear(userId);
        IdentityVerification overdue = pending(userId, "tangjinjie", "110101199001011232",
                LocalDateTime.now().minusHours(100));
        identityVerificationService.save(overdue);

        Users currentUser = usersService.getById(userId);
        assertNotNull(currentUser);
        IdentityVerification resubmitted = identityVerificationService.submit(
                currentUser, "张三", "110101199001011239");

        assertNotNull(resubmitted.getSubmitTime(), "重新提交必须刷新 submit_time");
        assertTrue(resubmitted.getSubmitTime().isAfter(LocalDateTime.now().minusMinutes(5)),
                "submit_time 必须被刷新为当前时间，实际: " + resubmitted.getSubmitTime());

        assertEquals(0, identityVerificationService.expireOverdue(72), "刷新后不应再被判超时");
        assertEquals("pending", identityVerificationService.getById(resubmitted.getId()).getStatus());
    }

    private void clear(Long userId) {
        identityVerificationService.remove(new LambdaQueryWrapper<IdentityVerification>()
                .eq(IdentityVerification::getUserId, userId));
    }

    private IdentityVerification pending(Long userId, String username, String idCardNo,
                                         LocalDateTime submitTime) {
        IdentityVerification verification = new IdentityVerification();
        verification.setUserId(userId);
        verification.setUsername(username);
        verification.setRealName("测试用户");
        verification.setIdCardNo(idCardNo);
        verification.setStatus("pending");
        verification.setCreateTime(submitTime);
        verification.setSubmitTime(submitTime);
        verification.setUpdateTime(submitTime);
        return verification;
    }
}
