package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        // 暂时使用MyBatis Plus的查询构造器，只查询预约记录的基本信息
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();

        // 同时查询作为租客和房东的预约
        queryWrapper.eq("tenant_id", userId).or().eq("landlord_id", userId);

        // 如果指定了状态，添加状态过滤
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }

        // 按照创建时间倒序排列
        queryWrapper.orderByDesc("create_time");

        return this.list(queryWrapper);
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