package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Mapper.AppointmentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Override
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
    public boolean cancelAppointment(Long id) {
        return updateAppointmentStatus(id, "canceled");
    }

    @Override
    public boolean deleteAppointment(Long id) {
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
