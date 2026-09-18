package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.LandlordApplication;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.LandlordApplicationMapper;
import com.springboot.springboothousemarket.dto.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 房东入驻申请闭环：
 * 注册(选房东意向)或租客主动申请 → pending → 管理员审核
 * → 通过：同一事务内升级用户角色为 LANDLORD + 通知
 * → 拒绝：通知申请人，可修改资料后重新提交
 */
@Service
public class LandlordApplicationServiceImpl extends ServiceImpl<LandlordApplicationMapper, LandlordApplication>
        implements LandlordApplicationService {

    private final UsersService usersService;
    private final NotificationDispatcher notificationDispatcher;

    public LandlordApplicationServiceImpl(UsersService usersService,
                                          NotificationDispatcher notificationDispatcher) {
        this.usersService = usersService;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Override
    @Transactional
    public LandlordApplication submit(Long userId, String username, String realName, String phone) {
        LandlordApplication existing = getByUserId(userId);
        if (existing != null) {
            if ("approved".equals(existing.getStatus())) {
                // 已是房东，无需重复申请
                return existing;
            }
            if ("pending".equals(existing.getStatus())) {
                return existing;
            }
            // rejected：允许修改资料后重新提交
            existing.setRealName(realName);
            existing.setPhone(phone);
            existing.setStatus("pending");
            existing.setReviewNote(null);
            existing.setReviewerId(null);
            existing.setReviewTime(null);
            existing.setUpdateTime(LocalDateTime.now());
            this.updateById(existing);
            return existing;
        }
        LandlordApplication application = new LandlordApplication();
        application.setUserId(userId);
        application.setUsername(username);
        application.setRealName(realName);
        application.setPhone(phone);
        application.setStatus("pending");
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        this.save(application);
        return application;
    }

    @Override
    @Transactional
    public LandlordApplication apply(Users currentUser, String realName, String phone) {
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (!"TENANT".equals(currentUser.getRole())) {
            throw new BusinessException("只有租客可以申请成为房东");
        }
        if (realName == null || realName.isBlank()) {
            throw new BusinessException("请填写真实姓名");
        }
        if (phone == null || phone.isBlank()) {
            throw new BusinessException("请填写联系电话");
        }
        return submit(currentUser.getId(), currentUser.getUsername(), realName.trim(), phone.trim());
    }

    @Override
    public LandlordApplication getByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<LandlordApplication>()
                .eq(LandlordApplication::getUserId, userId)
                .last("LIMIT 1"));
    }

    @Override
    public List<LandlordApplication> listByStatus(String status) {
        return this.list(new LambdaQueryWrapper<LandlordApplication>()
                .eq(status != null && !status.isBlank(), LandlordApplication::getStatus, status)
                .orderByDesc(LandlordApplication::getCreateTime));
    }

    @Override
    @Transactional
    public boolean approve(Long id, Long reviewerId, String note) {
        LandlordApplication application = this.getById(id);
        if (application == null || !"pending".equals(application.getStatus())) {
            return false;
        }
        boolean updated = this.lambdaUpdate()
                .eq(LandlordApplication::getId, id)
                .eq(LandlordApplication::getStatus, "pending")
                .set(LandlordApplication::getStatus, "approved")
                .set(LandlordApplication::getReviewerId, reviewerId)
                .set(LandlordApplication::getReviewNote, note)
                .set(LandlordApplication::getReviewTime, LocalDateTime.now())
                .set(LandlordApplication::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            return false;
        }
        // 审核通过必须真正改变角色：与审核单状态同一事务
        Users applicant = usersService.getUserById(application.getUserId());
        if (applicant == null) {
            throw new BusinessException("申请人不存在，审核中止");
        }
        applicant.setRole("LANDLORD");
        usersService.updateById(applicant);

        notificationDispatcher.dispatch(
                "LANDLORD_APPLICATION:" + id + ":LANDLORD_APPROVED",
                "LANDLORD",
                null,
                application.getUserId(),
                "LANDLORD_APPROVED",
                "房东入驻审核通过",
                "您的房东入驻申请已通过，账号已升级为房东，现在可以发布房源了",
                "LANDLORD_APPLICATION", id);
        return true;
    }

    @Override
    @Transactional
    public boolean reject(Long id, Long reviewerId, String note) {
        LandlordApplication application = this.getById(id);
        if (application == null || !"pending".equals(application.getStatus())) {
            return false;
        }
        boolean updated = this.lambdaUpdate()
                .eq(LandlordApplication::getId, id)
                .eq(LandlordApplication::getStatus, "pending")
                .set(LandlordApplication::getStatus, "rejected")
                .set(LandlordApplication::getReviewerId, reviewerId)
                .set(LandlordApplication::getReviewNote, note)
                .set(LandlordApplication::getReviewTime, LocalDateTime.now())
                .set(LandlordApplication::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            return false;
        }
        notificationDispatcher.dispatch(
                "LANDLORD_APPLICATION:" + id + ":LANDLORD_REJECTED",
                "LANDLORD",
                null,
                application.getUserId(),
                "LANDLORD_REJECTED",
                "房东入驻审核未通过",
                "很抱歉，您的房东入驻申请未通过：" + (note == null ? "" : note) + "。修改资料后可重新提交申请",
                "LANDLORD_APPLICATION", id);
        return true;
    }

    @Override
    public boolean hasApproved(Long userId) {
        return this.count(new LambdaQueryWrapper<LandlordApplication>()
                .eq(LandlordApplication::getUserId, userId)
                .eq(LandlordApplication::getStatus, "approved")) > 0;
    }
}
