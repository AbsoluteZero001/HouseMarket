package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.IdentityVerification;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.IdentityVerificationMapper;
import com.springboot.springboothousemarket.dto.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IdentityVerificationServiceImpl extends ServiceImpl<IdentityVerificationMapper, IdentityVerification>
        implements IdentityVerificationService {

    private final UsersService usersService;
    private final NotificationDispatcher notificationDispatcher;

    public IdentityVerificationServiceImpl(UsersService usersService,
                                           NotificationDispatcher notificationDispatcher) {
        this.usersService = usersService;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Override
    @Transactional
    public IdentityVerification submit(Users currentUser, String realName, String idCardNo) {
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (realName == null || realName.isBlank()) {
            throw new BusinessException("请填写真实姓名");
        }
        if (idCardNo == null || !idCardNo.trim().toUpperCase().matches("\\d{17}[\\dX]")) {
            throw new BusinessException("请输入18位有效身份证号");
        }
        String cardNo = idCardNo.trim().toUpperCase();

        // 同一身份证号只能认证一个账号（排除自己）
        Long conflict = lambdaQuery()
                .eq(IdentityVerification::getIdCardNo, cardNo)
                .ne(IdentityVerification::getUserId, currentUser.getId())
                .in(IdentityVerification::getStatus, List.of("pending", "approved"))
                .count();
        if (conflict > 0) {
            throw new BusinessException("该身份证号已被其他账号使用");
        }

        IdentityVerification existing = getByUserId(currentUser.getId());
        if (existing != null && "approved".equals(existing.getStatus())) {
            throw new BusinessException("您已完成实名认证，无需重复提交");
        }
        if (existing != null) {
            existing.setRealName(realName.trim());
            existing.setIdCardNo(cardNo);
            existing.setStatus("pending");
            existing.setReviewNote(null);
            existing.setReviewerId(null);
            existing.setReviewTime(null);
            existing.setUpdateTime(LocalDateTime.now());
            updateById(existing);
            return existing;
        }

        IdentityVerification verification = new IdentityVerification();
        verification.setUserId(currentUser.getId());
        verification.setUsername(currentUser.getUsername());
        verification.setRealName(realName.trim());
        verification.setIdCardNo(cardNo);
        verification.setStatus("pending");
        verification.setCreateTime(LocalDateTime.now());
        verification.setUpdateTime(LocalDateTime.now());
        save(verification);
        return verification;
    }

    @Override
    public IdentityVerification getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<IdentityVerification>()
                .eq(IdentityVerification::getUserId, userId)
                .last("LIMIT 1"));
    }

    @Override
    public List<IdentityVerification> listByStatus(String status) {
        return list(new LambdaQueryWrapper<IdentityVerification>()
                .eq(status != null && !status.isBlank(), IdentityVerification::getStatus, status)
                .orderByDesc(IdentityVerification::getCreateTime));
    }

    @Override
    @Transactional
    public boolean approve(Long id, Long reviewerId, String note) {
        IdentityVerification verification = getById(id);
        if (verification == null || !"pending".equals(verification.getStatus())) {
            return false;
        }
        boolean updated = lambdaUpdate()
                .eq(IdentityVerification::getId, id)
                .eq(IdentityVerification::getStatus, "pending")
                .set(IdentityVerification::getStatus, "approved")
                .set(IdentityVerification::getReviewerId, reviewerId)
                .set(IdentityVerification::getReviewNote, note)
                .set(IdentityVerification::getReviewTime, LocalDateTime.now())
                .set(IdentityVerification::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            return false;
        }
        // 通过后同一事务回写用户实名状态
        Users user = usersService.getUserById(verification.getUserId());
        if (user == null) {
            throw new BusinessException("申请人不存在，审核中止");
        }
        user.setRealName(verification.getRealName());
        user.setIdCardNo(verification.getIdCardNo());
        user.setRealNameVerified(1);
        user.setVerifiedTime(LocalDateTime.now());
        usersService.updateById(user);

        notificationDispatcher.dispatch(
                "IDENTITY:" + id + ":IDENTITY_APPROVED",
                "IDENTITY",
                null,
                verification.getUserId(),
                "IDENTITY_APPROVED",
                "实名认证已通过",
                "您的实名认证申请已通过，现在可以正常使用房东相关功能",
                "IDENTITY_VERIFICATION", id);
        return true;
    }

    @Override
    @Transactional
    public boolean reject(Long id, Long reviewerId, String note) {
        IdentityVerification verification = getById(id);
        if (verification == null || !"pending".equals(verification.getStatus())) {
            return false;
        }
        boolean updated = lambdaUpdate()
                .eq(IdentityVerification::getId, id)
                .eq(IdentityVerification::getStatus, "pending")
                .set(IdentityVerification::getStatus, "rejected")
                .set(IdentityVerification::getReviewerId, reviewerId)
                .set(IdentityVerification::getReviewNote, note)
                .set(IdentityVerification::getReviewTime, LocalDateTime.now())
                .set(IdentityVerification::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            return false;
        }
        notificationDispatcher.dispatch(
                "IDENTITY:" + id + ":IDENTITY_REJECTED",
                "IDENTITY",
                null,
                verification.getUserId(),
                "IDENTITY_REJECTED",
                "实名认证未通过",
                "很抱歉，您的实名认证申请未通过：" + (note == null ? "" : note) + "。修改资料后可重新提交",
                "IDENTITY_VERIFICATION", id);
        return true;
    }
}
