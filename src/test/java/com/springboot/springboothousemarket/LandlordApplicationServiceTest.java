package com.springboot.springboothousemarket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springboot.springboothousemarket.Entity.IdentityVerification;
import com.springboot.springboothousemarket.Entity.LandlordApplication;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.IdentityVerificationService;
import com.springboot.springboothousemarket.Service.LandlordApplicationService;
import com.springboot.springboothousemarket.Service.NotificationOutboxService;
import com.springboot.springboothousemarket.Service.RegisterRequestService;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class LandlordApplicationServiceTest {

    @Autowired
    private RegisterRequestService registerRequestService;

    @Autowired
    private LandlordApplicationService landlordApplicationService;

    @Autowired
    private NotificationOutboxService outboxService;

    @Autowired
    private IdentityVerificationService identityVerificationService;

    @Autowired
    private UsersService usersService;

    /**
     * 注册一个「房东意向」账号。
     * 注册时选择房东 = 同时提交一份 pending 状态的入驻申请单（角色此时仍是 TENANT）。
     */
    private Long registerLandlordIntent(String role) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test_" + role.toLowerCase() + "_"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        request.setPassword("123456");
        request.setRole(role);
        registerRequestService.register(request);
        assertNotNull(request.getId());
        return request.getId();
    }

    /**
     * 完成实名认证（人工审核通过），使申请人 real_name_verified = 1。
     * 这是房东入驻审核通过的前置条件。
     */
    private void completeRealNameVerification(Long userId) {
        Users user = usersService.getUserById(userId);
        String idCard = "11010119900307" + ThreadLocalRandom.current().nextInt(1000, 10000);
        IdentityVerification verification = identityVerificationService.submit(user, "张三", idCard);
        assertTrue(identityVerificationService.approve(verification.getId(), 1L, "核验通过"));
        assertEquals(1, usersService.getUserById(userId).getRealNameVerified().intValue());
    }

    @Test
    void landlordRegisterGoesToPendingAndCanBeApproved() {
        Long userId = registerLandlordIntent("LANDLORD");

        LandlordApplication application = landlordApplicationService.getByUserId(userId);
        assertNotNull(application);
        assertEquals("pending", application.getStatus());
        assertFalse(landlordApplicationService.hasApproved(userId));

        // 未完成实名认证时不允许通过审核（见 approveIsRejectedWhenApplicantNotRealNameVerified）
        completeRealNameVerification(userId);

        boolean approved = landlordApplicationService.approve(application.getId(), 1L, "审核通过");
        assertTrue(approved);
        assertTrue(landlordApplicationService.hasApproved(userId));
        assertEquals("LANDLORD", usersService.getUserById(userId).getRole());

        List<NotificationOutbox> rows = outboxService.list(
                new LambdaQueryWrapper<NotificationOutbox>()
                        .eq(NotificationOutbox::getBusinessType, "LANDLORD")
                        .eq(NotificationOutbox::getTargetUserId, userId));
        assertFalse(rows.isEmpty());
    }

    /**
     * 回归：未完成实名认证时，审核必须被拒绝，且不能留下「审核单已通过、账号却永远发不了房源」的死路状态。
     */
    @Test
    void approveIsRejectedWhenApplicantNotRealNameVerified() {
        Long userId = registerLandlordIntent("LANDLORD");
        LandlordApplication application = landlordApplicationService.getByUserId(userId);
        assertEquals("pending", application.getStatus());

        BusinessException error = assertThrows(BusinessException.class,
                () -> landlordApplicationService.approve(application.getId(), 1L, "审核通过"));
        assertTrue(error.getMessage().contains("实名认证"),
                "错误信息应明确指向实名认证前置条件，实际为：" + error.getMessage());

        // 审核未通过：申请单状态、审核结果、用户角色都必须保持原样
        LandlordApplication after = landlordApplicationService.getByUserId(userId);
        assertEquals("pending", after.getStatus());
        assertFalse(landlordApplicationService.hasApproved(userId));
        assertEquals("TENANT", usersService.getUserById(userId).getRole());
    }

    /**
     * 回归：已是房东的用户仍要能维护入驻资料。
     * 旧实现对 approved 状态直接 return，导致 LANDLORD 用户永远无法更新联系资料（死路）。
     */
    @Test
    void approvedLandlordCanStillUpdateApplicationInfo() {
        Long userId = registerLandlordIntent("LANDLORD");
        completeRealNameVerification(userId);
        LandlordApplication application = landlordApplicationService.getByUserId(userId);
        assertTrue(landlordApplicationService.approve(application.getId(), 1L, "审核通过"));

        Users landlord = usersService.getUserById(userId);
        assertEquals("LANDLORD", landlord.getRole());

        LandlordApplication updated = landlordApplicationService.apply(landlord, "李四", "13900001111");
        assertEquals("approved", updated.getStatus());
        assertEquals("李四", updated.getRealName());
        assertEquals("13900001111", updated.getPhone());

        LandlordApplication reloaded = landlordApplicationService.getByUserId(userId);
        assertEquals("approved", reloaded.getStatus());
        assertEquals("李四", reloaded.getRealName());
        assertEquals("13900001111", reloaded.getPhone());
        // 角色不得被回退
        assertEquals("LANDLORD", usersService.getUserById(userId).getRole());
    }

    /**
     * 租客主动申请成为房东：状态应为 pending，资料应写入申请单。
     */
    @Test
    void tenantCanSubmitLandlordApplication() {
        Long userId = registerLandlordIntent("TENANT");
        Users tenant = usersService.getUserById(userId);
        assertEquals("TENANT", tenant.getRole());

        LandlordApplication application = landlordApplicationService.apply(tenant, "王五", "13700002222");
        assertEquals("pending", application.getStatus());
        assertEquals("王五", application.getRealName());
        assertEquals("13700002222", application.getPhone());
        assertNotNull(landlordApplicationService.getByUserId(userId));
    }
}