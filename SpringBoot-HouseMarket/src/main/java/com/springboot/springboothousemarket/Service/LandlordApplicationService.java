package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.LandlordApplication;
import com.springboot.springboothousemarket.Entity.Users;

import java.util.List;

public interface LandlordApplicationService extends IService<LandlordApplication> {

    /**
     * 提交/重新提交房东入驻申请。
     * - 无申请记录：新建 pending；
     * - 已有 pending：幂等返回；
     * - 已有 approved：拒绝（已是房东）；
     * - 已有 rejected：允许修改资料后重新提交（重置为 pending）。
     */
    LandlordApplication submit(Long userId, String username, String realName, String phone);

    /**
     * 租客主动申请成为房东（业务校验：必须 TENANT 角色）。
     */
    LandlordApplication apply(Users currentUser, String realName, String phone);

    LandlordApplication getByUserId(Long userId);

    List<LandlordApplication> listByStatus(String status);

    /**
     * 审核通过：同一事务内 审核单状态 + 用户角色升级 LANDLORD + 通知落库/入队。
     */
    boolean approve(Long id, Long reviewerId, String note);

    /**
     * 审核拒绝：记录审核信息并通知申请人（可修改后重新申请）。
     */
    boolean reject(Long id, Long reviewerId, String note);

    boolean hasApproved(Long userId);
}
