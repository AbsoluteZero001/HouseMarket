package com.springboot.springboothousemarket.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 实名认证审核 SLA 巡检：把「提交后长时间没人审核」的申请自动置为已拒绝，并通知申请人。
 *
 * 背景：实名认证是纯人工审核，没有任何时限约束。管理员不处理时申请会一直挂在 pending，
 * 用户既拿不到结果，也不知道还要等多久。这里用可配置的 SLA（默认 72 小时）兜底，
 * 保证每一条申请最终都有终态，且用户能重新提交。
 */
@Slf4j
@Component
public class IdentityVerificationExpiryJob {

    private final IdentityVerificationService identityVerificationService;

    @Value("${app.identity.verify-expire-hours:72}")
    private int expireHours;

    @Value("${app.identity.verify-expiry-enabled:true}")
    private boolean enabled;

    public IdentityVerificationExpiryJob(IdentityVerificationService identityVerificationService) {
        this.identityVerificationService = identityVerificationService;
    }

    @Scheduled(fixedDelayString = "${app.identity.verify-expiry-interval-ms:600000}")
    public void expireOverdueApplications() {
        if (!enabled) {
            return;
        }
        try {
            int rejected = identityVerificationService.expireOverdue(expireHours);
            if (rejected > 0) {
                log.info("实名认证审核超时自动拒绝 {} 条申请（SLA={}小时）", rejected, expireHours);
            }
        } catch (Exception e) {
            // 定时任务异常不能拖垮整个应用；下一轮巡检会继续处理
            log.warn("实名认证审核超时巡检失败, reason={}", e.getMessage());
        }
    }
}
