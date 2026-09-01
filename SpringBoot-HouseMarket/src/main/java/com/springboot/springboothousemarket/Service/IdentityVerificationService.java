package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.IdentityVerification;
import com.springboot.springboothousemarket.Entity.Users;

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
}
