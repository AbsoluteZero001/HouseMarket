package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.AppointmentMapper;
import com.springboot.springboothousemarket.Mapper.HousesMapper;
import com.springboot.springboothousemarket.common.AppointmentStatus;
import com.springboot.springboothousemarket.common.HouseStatus;
import com.springboot.springboothousemarket.dto.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final AppointmentFlowService flowService;
    private final HousesMapper housesMapper;
    private final NotificationDispatcher notificationDispatcher;

    @Value("${app.appointment.pending-expire-hours:24}")
    private int pendingExpireHours;

    public AppointmentServiceImpl(AppointmentFlowService flowService,
                                  HousesMapper housesMapper,
                                  NotificationDispatcher notificationDispatcher) {
        this.flowService = flowService;
        this.housesMapper = housesMapper;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public Appointment createAppointment(Appointment appointment, Users tenant) {
        if (tenant == null || !"TENANT".equals(tenant.getRole())) {
            throw new BusinessException("只有租客才能创建预约");
        }
        if (appointment.getTime() == null) {
            throw new BusinessException("预约时间不能为空");
        }
        if (appointment.getLocation() == null || appointment.getLocation().isBlank()) {
            throw new BusinessException("预约地点不能为空");
        }
        // 预约时间不能是过去
        if (!appointment.getTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("预约时间必须晚于当前时间");
        }

        // 幂等：同一 requestId 重复提交返回已有预约
        if (appointment.getRequestId() != null && !appointment.getRequestId().isBlank()) {
            Appointment existing = this.lambdaQuery()
                    .eq(Appointment::getRequestId, appointment.getRequestId())
                    .one();
            if (existing != null) {
                return existing;
            }
        }

        // 悲观锁锁定房源行，串行化同一房源的预约创建，配合冲突检测消除并发竞态
        Houses house = housesMapper.selectByIdForUpdate(appointment.getHouseId());
        if (house == null || Integer.valueOf(1).equals(house.getIsDeleted())) {
            throw new BusinessException("房源不存在");
        }
        if (!HouseStatus.NORMAL.equals(house.getStatus())) {
            throw new BusinessException("该房源当前不可预约（仅已上架房源可预约）");
        }
        if (tenant.getId().equals(house.getLandlordId())) {
            throw new BusinessException("不能预约自己的房源");
        }

        appointment.setTenantId(tenant.getId());
        appointment.setLandlordId(house.getLandlordId());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setVersion(0);
        appointment.setCreateTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());

        // 同一房源同一时间只允许一个有效（pending/approved）预约
        long conflictCount = this.lambdaQuery()
                .eq(Appointment::getHouseId, appointment.getHouseId())
                .eq(Appointment::getTime, appointment.getTime())
                .in(Appointment::getStatus, AppointmentStatus.OCCUPYING)
                .count();
        if (conflictCount > 0) {
            throw new BusinessException("该房源在预约时间段已有预约，请更换时间");
        }

        try {
            this.save(appointment);
        } catch (DuplicateKeyException duplicate) {
            if (appointment.getRequestId() != null) {
                Appointment existing = this.lambdaQuery()
                        .eq(Appointment::getRequestId, appointment.getRequestId())
                        .one();
                if (existing != null) {
                    return existing;
                }
            }
            throw new BusinessException("预约提交冲突，请重试");
        }

        recordFlow(appointment.getId(), null, "published", "PUBLISH",
                house.getLandlordId(), "LANDLORD", "房源已发布上线");
        recordFlow(appointment.getId(), null, AppointmentStatus.PENDING, "BOOK",
                tenant.getId(), "TENANT", "租客提交看房预约");

        notifyAppointment(appointment, AppointmentStatus.PENDING, "APPOINTMENT_CREATED",
                "有新预约申请待处理",
                "租客「" + displayName(tenant) + "」预约了「" + house.getTitle() + "」，请及时处理",
                house.getLandlordId());
        return appointment;
    }

    @Override
    public List<Appointment> getAppointmentsByUserIdAndStatus(Long userId, String status) {
        return this.baseMapper.selectAppointmentsWithDetails(userId, status);
    }

    @Override
    public List<Appointment> getAppointmentsByLandlordId(Long landlordId, String status) {
        return this.baseMapper.selectAppointmentsByLandlordIdWithDetails(landlordId, status);
    }

    @Override
    public List<Appointment> getAllAppointments(String status) {
        if (status != null && !status.isEmpty()) {
            return this.lambdaQuery()
                    .eq(Appointment::getStatus, status)
                    .orderByDesc(Appointment::getCreateTime)
                    .list();
        }
        return this.lambdaQuery()
                .orderByDesc(Appointment::getCreateTime)
                .list();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public boolean approve(Long id, Users operator) {
        Appointment appointment = requireTransitionable(id, operator, AppointmentStatus.PENDING, true);
        boolean updated = transition(appointment, AppointmentStatus.APPROVED, "APPROVE",
                operator.getId(), "LANDLORD", "房东审批通过");
        if (updated) {
            notifyAppointment(appointment, AppointmentStatus.APPROVED, "APPOINTMENT_APPROVED",
                    "预约已批准",
                    "您预约的「" + houseTitle(appointment.getHouseId()) + "」看房申请已被房东批准，预约时间：" + appointment.getTime(),
                    appointment.getTenantId());
        }
        return updated;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public boolean reject(Long id, Users operator) {
        Appointment appointment = requireTransitionable(id, operator, AppointmentStatus.PENDING, true);
        boolean updated = transition(appointment, AppointmentStatus.REJECTED, "REJECT",
                operator.getId(), "LANDLORD", "房东拒绝本次预约");
        if (updated) {
            notifyAppointment(appointment, AppointmentStatus.REJECTED, "APPOINTMENT_REJECTED",
                    "预约已拒绝",
                    "很抱歉，您对「" + houseTitle(appointment.getHouseId()) + "」的预约被房东拒绝",
                    appointment.getTenantId());
        }
        return updated;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public boolean cancel(Long id, Users operator) {
        Appointment appointment = getById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        boolean isTenant = appointment.getTenantId().equals(operator.getId());
        boolean isLandlord = appointment.getLandlordId().equals(operator.getId());
        if (!isTenant && !isLandlord && !"ADMIN".equals(operator.getRole())) {
            throw new BusinessException("没有权限取消该预约");
        }
        if (!AppointmentStatus.PENDING.equals(appointment.getStatus())
                && !AppointmentStatus.APPROVED.equals(appointment.getStatus())) {
            throw new BusinessException("只能取消待处理或已批准的预约");
        }
        boolean updated = transition(appointment, AppointmentStatus.CANCELED, "CANCEL",
                operator.getId(), operator.getRole(), isTenant ? "租客取消预约" : "房东取消预约");
        if (updated) {
            notifyAppointment(appointment, AppointmentStatus.CANCELED, "APPOINTMENT_CANCELED",
                    "预约已取消",
                    "「" + houseTitle(appointment.getHouseId()) + "」的看房预约已被"
                            + (isTenant ? "租客" : "房东") + "取消",
                    isTenant ? appointment.getLandlordId() : appointment.getTenantId());
        }
        return updated;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public boolean complete(Long id, Users operator) {
        Appointment appointment = requireTransitionable(id, operator, AppointmentStatus.APPROVED, false);
        boolean updated = transition(appointment, AppointmentStatus.COMPLETED, "COMPLETE",
                operator.getId(), "LANDLORD", "看房完成，预约闭环结束");
        if (updated) {
            notifyAppointment(appointment, AppointmentStatus.COMPLETED, "APPOINTMENT_COMPLETED",
                    "看房预约已完成",
                    "您在「" + houseTitle(appointment.getHouseId()) + "」的看房已完成",
                    appointment.getTenantId());
        }
        return updated;
    }

    /**
     * 定时任务：pending 超时自动过期。
     * 过期条件：看房时间已过，或创建超过 app.appointment.pending-expire-hours 小时仍未处理。
     */
    @Override
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public int expireOverdueAppointments() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> overdue = this.lambdaQuery()
                .eq(Appointment::getStatus, AppointmentStatus.PENDING)
                .and(wrapper -> wrapper
                        .lt(Appointment::getTime, now)
                        .or()
                        .lt(Appointment::getCreateTime, now.minusHours(pendingExpireHours)))
                .list();
        int count = 0;
        for (Appointment appointment : overdue) {
            boolean updated = transition(appointment, AppointmentStatus.EXPIRED, "EXPIRE",
                    null, "SYSTEM", "预约超时未处理，系统自动过期");
            if (updated) {
                notifyAppointment(appointment, AppointmentStatus.EXPIRED, "APPOINTMENT_EXPIRED",
                        "预约已过期",
                        "您对「" + houseTitle(appointment.getHouseId()) + "」的预约因超时未处理已自动过期，可重新发起预约",
                        appointment.getTenantId());
                count++;
            }
        }
        if (count > 0) {
            log.info("预约超时过期完成, count={}", count);
        }
        return count;
    }

    @Override
    public List<AppointmentFlow> getFlows(Long appointmentId) {
        return flowService.getFlowsByAppointmentId(appointmentId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public boolean deleteAppointment(Long id) {
        Appointment appointment = getById(id);
        if (appointment == null) {
            return false;
        }
        return removeById(id);
    }

    // ------------------------------------------------------------------
    // 内部方法：状态流转 + 轨迹 + 通知 必须在同一事务内
    // ------------------------------------------------------------------

    private Appointment requireTransitionable(Long id, Users operator, String requiredStatus, boolean requireLandlordOwner) {
        Appointment appointment = getById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if (requireLandlordOwner) {
            if (!"LANDLORD".equals(operator.getRole())) {
                throw new BusinessException("只有房东才能执行该操作");
            }
            if (!appointment.getLandlordId().equals(operator.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("只能操作自己房源的预约");
            }
        } else {
            if (!"LANDLORD".equals(operator.getRole()) || !appointment.getLandlordId().equals(operator.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("只有房源所属房东才能执行该操作");
            }
        }
        if (!requiredStatus.equals(appointment.getStatus())) {
            throw new BusinessException("当前状态不允许该操作（要求状态：" + requiredStatus + "）");
        }
        return appointment;
    }

    /**
     * 条件更新（乐观锁 version）保证并发下状态只被流转一次。
     */
    private boolean transition(Appointment current, String toStatus, String action,
                               Long operatorId, String operatorRole, String remark) {
        Appointment update = new Appointment();
        update.setId(current.getId());
        update.setStatus(toStatus);
        update.setUpdateTime(LocalDateTime.now());
        update.setVersion(current.getVersion() == null ? 0 : current.getVersion());
        boolean updated = this.updateById(update);
        if (!updated) {
            throw new BusinessException("预约状态已被其他操作变更，请刷新后重试");
        }
        recordFlow(current.getId(), current.getStatus(), toStatus, action, operatorId, operatorRole, remark);
        return true;
    }

    private void recordFlow(Long appointmentId, String fromStatus, String toStatus, String action,
                            Long operatorId, String operatorRole, String remark) {
        AppointmentFlow flow = new AppointmentFlow();
        flow.setAppointmentId(appointmentId);
        flow.setFromStatus(fromStatus);
        flow.setToStatus(toStatus);
        flow.setAction(action);
        flow.setOperatorId(operatorId);
        flow.setOperatorRole(operatorRole);
        flow.setRemark(remark);
        flowService.record(flow);
    }

    private void notifyAppointment(Appointment appointment, String status, String eventType,
                                   String title, String content, Long targetUserId) {
        notificationDispatcher.dispatch(
                appointment.getId() + ":" + eventType,
                "APPOINTMENT",
                appointment.getId(),
                targetUserId,
                eventType,
                title,
                content,
                "APPOINTMENT",
                appointment.getId());
        recordFlow(appointment.getId(), status, status, "NOTIFY", null, "SYSTEM", "已通过通知中心告知对方");
    }

    private String houseTitle(Long houseId) {
        Houses house = housesMapper.selectById(houseId);
        return house != null ? house.getTitle() : "房源";
    }

    private String displayName(Users user) {
        if (user == null) {
            return "租客";
        }
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getUsername();
    }
}
