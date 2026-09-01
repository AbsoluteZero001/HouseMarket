package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;
import com.springboot.springboothousemarket.Entity.ChatMessage;
import com.springboot.springboothousemarket.Entity.Favorites;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.IdentityVerification;
import com.springboot.springboothousemarket.Entity.LandlordApplication;
import com.springboot.springboothousemarket.Entity.Notification;
import com.springboot.springboothousemarket.Entity.NotificationOutbox;
import com.springboot.springboothousemarket.Mapper.AppointmentFlowMapper;
import com.springboot.springboothousemarket.Mapper.AppointmentMapper;
import com.springboot.springboothousemarket.Mapper.ChatMessageMapper;
import com.springboot.springboothousemarket.Mapper.FavoritesMapper;
import com.springboot.springboothousemarket.Mapper.HousesMapper;
import com.springboot.springboothousemarket.Mapper.IdentityVerificationMapper;
import com.springboot.springboothousemarket.Mapper.LandlordApplicationMapper;
import com.springboot.springboothousemarket.Mapper.NotificationMapper;
import com.springboot.springboothousemarket.Mapper.NotificationOutboxMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户数据级联清理：删除用户时清理其名下房源、预约、收藏、聊天、通知、申请单，
 * 避免孤儿数据。仅在用户删除场景调用，全部处于同一事务。
 */
@Slf4j
@Service
public class UserCleanupService {

    private final HousesMapper housesMapper;
    private final HouseImageService houseImageService;
    private final FavoritesMapper favoritesMapper;
    private final AppointmentMapper appointmentMapper;
    private final AppointmentFlowMapper appointmentFlowMapper;
    private final NotificationOutboxMapper outboxMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final NotificationMapper notificationMapper;
    private final LandlordApplicationMapper landlordApplicationMapper;
    private final IdentityVerificationMapper identityVerificationMapper;

    public UserCleanupService(HousesMapper housesMapper,
                              HouseImageService houseImageService,
                              FavoritesMapper favoritesMapper,
                              AppointmentMapper appointmentMapper,
                              AppointmentFlowMapper appointmentFlowMapper,
                              NotificationOutboxMapper outboxMapper,
                              ChatMessageMapper chatMessageMapper,
                              NotificationMapper notificationMapper,
                              LandlordApplicationMapper landlordApplicationMapper,
                              IdentityVerificationMapper identityVerificationMapper) {
        this.housesMapper = housesMapper;
        this.houseImageService = houseImageService;
        this.favoritesMapper = favoritesMapper;
        this.appointmentMapper = appointmentMapper;
        this.appointmentFlowMapper = appointmentFlowMapper;
        this.outboxMapper = outboxMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.notificationMapper = notificationMapper;
        this.landlordApplicationMapper = landlordApplicationMapper;
        this.identityVerificationMapper = identityVerificationMapper;
    }

    @Transactional
    public void cleanupUserData(Long userId) {
        // 1. 名下房源：清理图片(含文件)、收藏、预约(轨迹/通知/记录)、聊天房源引用，软删房源行
        List<Houses> ownedHouses = housesMapper.selectList(
                new LambdaQueryWrapper<Houses>().eq(Houses::getLandlordId, userId));
        for (Houses house : ownedHouses) {
            houseImageService.deleteByHouseId(house.getId());
            favoritesMapper.delete(new LambdaQueryWrapper<Favorites>()
                    .eq(Favorites::getHouseId, house.getId()));
            deleteAppointmentsOfHouse(house.getId());
            chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                    .eq(ChatMessage::getHouseId, house.getId())
                    .set(ChatMessage::getHouseId, null));
            Houses softDelete = new Houses();
            softDelete.setId(house.getId());
            softDelete.setIsDeleted(1);
            housesMapper.updateById(softDelete);
        }

        // 2. 作为租客的预约
        List<Appointment> tenantAppointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getTenantId, userId));
        for (Appointment appointment : tenantAppointments) {
            deleteAppointmentFully(appointment.getId());
        }

        // 3. 收藏、聊天、通知、各类申请单、定向 Outbox
        favoritesMapper.delete(new LambdaQueryWrapper<Favorites>()
                .eq(Favorites::getUserId, userId));
        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSenderId, userId)
                .or()
                .eq(ChatMessage::getReceiverId, userId));
        notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId));
        landlordApplicationMapper.delete(new LambdaQueryWrapper<LandlordApplication>()
                .eq(LandlordApplication::getUserId, userId));
        identityVerificationMapper.delete(new LambdaQueryWrapper<IdentityVerification>()
                .eq(IdentityVerification::getUserId, userId));
        outboxMapper.delete(new LambdaQueryWrapper<NotificationOutbox>()
                .eq(NotificationOutbox::getTargetUserId, userId));
    }

    private void deleteAppointmentsOfHouse(Long houseId) {
        List<Appointment> appointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getHouseId, houseId));
        for (Appointment appointment : appointments) {
            deleteAppointmentFully(appointment.getId());
        }
    }

    private void deleteAppointmentFully(Long appointmentId) {
        outboxMapper.delete(new LambdaQueryWrapper<NotificationOutbox>()
                .eq(NotificationOutbox::getAppointmentId, appointmentId));
        appointmentFlowMapper.delete(new LambdaQueryWrapper<AppointmentFlow>()
                .eq(AppointmentFlow::getAppointmentId, appointmentId));
        appointmentMapper.deleteById(appointmentId);
    }
}
