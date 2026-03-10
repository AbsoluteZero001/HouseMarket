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
            // updateTime字段已从实体类中移除，因为数据库中没有这个字段
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
}