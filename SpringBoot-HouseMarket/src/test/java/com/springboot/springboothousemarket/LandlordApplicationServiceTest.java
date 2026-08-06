package com.springboot.springboothousemarket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.springboot.springboothousemarket.Entity.LandlordApplication;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Service.LandlordApplicationService;
import com.springboot.springboothousemarket.Service.NotificationOutboxService;
import com.springboot.springboothousemarket.Service.RegisterRequestService;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void landlordRegisterGoesToPendingAndCanBeApproved() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("test_landlord_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        request.setPassword("123456");
        request.setRole("LANDLORD");

        registerRequestService.register(request);

        assertNotNull(request.getId());
        LandlordApplication application = landlordApplicationService.getByUserId(request.getId());
        assertNotNull(application);
        assertEquals("pending", application.getStatus());
        assertFalse(landlordApplicationService.hasApproved(request.getId()));

        boolean approved = landlordApplicationService.approve(application.getId(), 1L, "审核通过");
        assertTrue(approved);
        assertTrue(landlordApplicationService.hasApproved(request.getId()));

        List<NotificationOutbox> rows = outboxService.list(
                new LambdaQueryWrapper<NotificationOutbox>()
                        .eq(NotificationOutbox::getBusinessType, "LANDLORD")
                        .eq(NotificationOutbox::getTargetUserId, request.getId()));
        assertFalse(rows.isEmpty());
    }
}
