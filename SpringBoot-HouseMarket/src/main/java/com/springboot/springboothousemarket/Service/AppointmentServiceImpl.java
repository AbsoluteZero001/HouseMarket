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
        this.save(appointment);
        return appointment;
    }

    @Override
    public List<Appointment> getAppointmentsByUserIdAndStatus(Long userId, String status) {
        // 这里应该实现具体的查询逻辑，目前只是示意
        return this.list();
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
}