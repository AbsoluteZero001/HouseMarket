package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springboothousemarket.Entity.LandlordApplication;
import com.springboot.springboothousemarket.Mapper.LandlordApplicationMapper;
import com.springboot.springboothousemarket.dto.AppointmentMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LandlordApplicationServiceImpl extends ServiceImpl<LandlordApplicationMapper, LandlordApplication>
        implements LandlordApplicationService {

    private final NotificationOutboxService outboxService;
    private final ObjectMapper objectMapper;

    public LandlordApplicationServiceImpl(NotificationOutboxService outboxService, ObjectMapper objectMapper) {
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public LandlordApplication submit(Long userId, String username, String realName, String phone) {
        LandlordApplication existing = getByUserId(userId);
        if (existing != null) {
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
        boolean updated = this.lambdaUpdate()
                .eq(LandlordApplication::getId, id)
                .eq(LandlordApplication::getStatus, "pending")
                .set(LandlordApplication::getStatus, "approved")
                .set(LandlordApplication::getReviewerId, reviewerId)
                .set(LandlordApplication::getReviewNote, note)
                .set(LandlordApplication::getReviewTime, LocalDateTime.now())
                .update();
        if (updated) {
            LandlordApplication application = this.getById(id);
            enqueueReviewNotification(application, "LANDLORD_APPROVED", "房东入驻审核通过，现在可以发布房源了");
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean reject(Long id, Long reviewerId, String note) {
        boolean updated = this.lambdaUpdate()
                .eq(LandlordApplication::getId, id)
                .eq(LandlordApplication::getStatus, "pending")
                .set(LandlordApplication::getStatus, "rejected")
                .set(LandlordApplication::getReviewerId, reviewerId)
                .set(LandlordApplication::getReviewNote, note)
                .set(LandlordApplication::getReviewTime, LocalDateTime.now())
                .update();
        if (updated) {
            LandlordApplication application = this.getById(id);
            enqueueReviewNotification(application, "LANDLORD_REJECTED", "房东入驻审核未通过：" + (note == null ? "" : note));
        }
        return updated;
    }

    @Override
    public boolean hasApproved(Long userId) {
        return this.count(new LambdaQueryWrapper<LandlordApplication>()
                .eq(LandlordApplication::getUserId, userId)
                .eq(LandlordApplication::getStatus, "approved")) > 0;
    }

    private void enqueueReviewNotification(LandlordApplication application, String eventType, String message) {
        AppointmentMessage msg = new AppointmentMessage();
        msg.setStatus(eventType);
        msg.setTargetUserId(application.getUserId());
        msg.setMessage(message);
        try {
            String payload = objectMapper.writeValueAsString(msg);
            outboxService.enqueue(
                    "LANDLORD_APPLICATION:" + application.getId() + ":" + eventType,
                    "LANDLORD",
                    null,
                    eventType,
                    payload,
                    application.getUserId());
        } catch (JsonProcessingException ignored) {
            // 通知尽力投递，不阻塞审核主流程
        }
    }
}
