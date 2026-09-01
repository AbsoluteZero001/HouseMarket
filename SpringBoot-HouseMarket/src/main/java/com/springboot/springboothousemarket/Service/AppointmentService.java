package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;
import com.springboot.springboothousemarket.Entity.Users;

import java.util.List;

/**
 * 预约业务：状态机统一收敛到 Service 层，
 * 每次状态变化在同一个事务内完成：状态更新 + 流程轨迹 + 用户通知落库 + Outbox 入队。
 * 状态机：pending → approved/rejected/expired；approved → completed/canceled；pending → canceled
 */
public interface AppointmentService extends IService<Appointment> {

    /**
     * 租客创建预约（业务校验：房源已上架、不能预约自己的房源、时间必须晚于当前、同一时段不可重复占用）。
     * 通过房源行悲观锁 + 冲突检测解决并发预约竞态。
     */
    Appointment createAppointment(Appointment appointment, Users tenant);

    List<Appointment> getAppointmentsByUserIdAndStatus(Long userId, String status);

    List<Appointment> getAppointmentsByLandlordId(Long landlordId, String status);

    List<Appointment> getAllAppointments(String status);

    boolean approve(Long id, Users operator);

    boolean reject(Long id, Users operator);

    boolean cancel(Long id, Users operator);

    boolean complete(Long id, Users operator);

    /**
     * 定时任务：过期未处理的预约自动流转 expired（看房时间已过或创建超过 N 小时未处理）。
     */
    int expireOverdueAppointments();

    List<AppointmentFlow> getFlows(Long appointmentId);

    boolean deleteAppointment(Long id);
}
