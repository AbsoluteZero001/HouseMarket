package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Mapper.AppointmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final AppointmentFlowService flowService;
    private final HousesService housesService;

    public AppointmentServiceImpl(AppointmentFlowService flowService, HousesService housesService) {
        this.flowService = flowService;
        this.housesService = housesService;
    }

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        appointment.setStatus("pending"); // 默认状态为待处理
        appointment.setCreateTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());
        long conflictCount = this.lambdaQuery()
                .eq(Appointment::getHouseId, appointment.getHouseId())
                .eq(Appointment::getTime, appointment.getTime())
                .in(Appointment::getStatus, "pending", "approved")
                .count();
        if (conflictCount > 0) {
            throw new RuntimeException("该房源在预约时间段已有预约");
        }
        this.save(appointment);

        Houses house = housesService.getHouseById(appointment.getHouseId());
        if (house != null && house.getCreateTime() != null) {
            AppointmentFlow publishFlow = new AppointmentFlow();
            publishFlow.setAppointmentId(appointment.getId());
            publishFlow.setFromStatus(null);
            publishFlow.setToStatus("published");
            publishFlow.setAction("PUBLISH");
            publishFlow.setOperatorId(house.getLandlordId());
            publishFlow.setOperatorRole("LANDLORD");
            publishFlow.setRemark("房源已发布上线");
            publishFlow.setCreateTime(house.getCreateTime());
            flowService.record(publishFlow);
        }
        recordFlow(appointment.getId(), null, "pending", "BOOK",
                appointment.getTenantId(), "TENANT", "租客提交看房预约");
        recordNotification(appointment.getId(), "pending",
                appointment.getLandlordId(), "LANDLORD", "已通知房东处理预约");
        return appointment;
    }

    @Override
    public List<Appointment> getAppointmentsByUserIdAndStatus(Long userId, String status) {
        // 使用自定义的mapper方法，查询预约记录并关联房屋、租客和房东信息
        return this.baseMapper.selectAppointmentsWithDetails(userId, status);
    }

    @Override
    public List<Appointment> getAppointmentsByLandlordId(Long landlordId, String status) {
        // 使用自定义的mapper方法，查询房东收到的预约记录并关联房屋、租客和房东信息
        return this.baseMapper.selectAppointmentsByLandlordIdWithDetails(landlordId, status);
    }

    @Override
    public boolean updateAppointmentStatus(Long id, String status) {
        Appointment appointment = this.getById(id);
        if (appointment != null) {
            appointment.setStatus(status);
            appointment.setUpdateTime(LocalDateTime.now());
            return this.updateById(appointment);
        }
        return false;
    }

    @Override
    @Transactional
    public boolean transitionStatus(Long id, String expectedStatus, String toStatus, String action,
                                    Long operatorId, String operatorRole, String remark) {
        boolean updated = this.lambdaUpdate()
                .eq(Appointment::getId, id)
                .eq(Appointment::getStatus, expectedStatus)
                .set(Appointment::getStatus, toStatus)
                .set(Appointment::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            return false;
        }
        recordFlow(id, expectedStatus, toStatus, action, operatorId, operatorRole, remark);
        return true;
    }

    @Override
    public void recordNotification(Long appointmentId, String status, Long operatorId, String operatorRole, String message) {
        recordFlow(appointmentId, status, status, "NOTIFY", operatorId, operatorRole, message);
    }

    @Override
    public List<AppointmentFlow> getFlows(Long appointmentId) {
        return flowService.getFlowsByAppointmentId(appointmentId);
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

    @Override
    public boolean cancelAppointment(Long id) {
        return updateAppointmentStatus(id, "canceled");
    }

    @Override
    @Transactional
    public boolean deleteAppointment(Long id) {
        flowService.deleteByAppointmentId(id);
        return this.removeById(id);
    }

    @Override
    public List<Appointment> getAllAppointments(String status) {
        // 使用自定义查询方法获取所有预约
        if (status != null && !status.isEmpty()) {
            return this.lambdaQuery()
                    .eq(Appointment::getStatus, status)
                    .orderByDesc(Appointment::getCreateTime)
                    .list();
        } else {
            return this.lambdaQuery()
                    .orderByDesc(Appointment::getCreateTime)
                    .list();
        }
    }
}
