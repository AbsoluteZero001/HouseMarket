package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.IdentityVerification;
import com.springboot.springboothousemarket.Entity.Users;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实名认证（人工审核制）：未认证 → 待审核 → 已认证 / 已拒绝（可重新提交）。
 * 说明：本系统不接入公安/OCR 实名校验，"实名认证"定位为资料提交 + 管理员人工核验。
 */
public interface IdentityVerificationService extends IService<IdentityVerification> {

    /**
     * 提交/重新提交实名认证申请（同一用户仅保留一条记录）。
     */
    IdentityVerification submit(Users currentUser, String realName, String idCardNo);

    IdentityVerification getByUserId(Long userId);

    List<IdentityVerification> listByStatus(String status);

    /**
     * 审核通过：同一事务内回写 sysuser 实名状态 + 通知申请人。
     */
    boolean approve(Long id, Long reviewerId, String note);

    /**
     * 审核拒绝：申请人可修改后重新提交。
     */
    boolean reject(Long id, Long reviewerId, String note);

    /**
     * 审核 SLA 超时处理：把「提交时间距今超过 expireHours 且仍处于 pending」的申请
     * 自动置为 rejected，并通知申请人（可修改后重新提交）。
     *
     * 人工审核没有时限约束时，申请会无限期挂起，用户既拿不到结果、也看不出还要等多久；
     * 加上超时自动拒绝后，SLA 对外可见且有终态。
     *
     * @return 本次实际被自动拒绝的申请数
     */
    int expireOverdue(int expireHours);

    /**
     * 取「本次提交时间」：优先 submit_time（重新提交会刷新），
     * 存量数据 submit_time 为空时回落到 create_time。
     */
    default LocalDateTime resolveSubmitTime(IdentityVerification verification) {
        if (verification == null) {
            return null;
        }
        return verification.getSubmitTime() != null ? verification.getSubmitTime() : verification.getCreateTime();
    }
}
