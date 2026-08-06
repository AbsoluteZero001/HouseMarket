package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Appointment;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;

import java.util.List;

public interface AppointmentService extends IService<Appointment> {

    /**
     * 创建预约
     *
     * @param appointment 预约信息
     * @return 创建后的预约信息
     */
    Appointment createAppointment(Appointment appointment);

    /**
     * 根据用户ID获取预约列表，包括作为租客和房东的预约
     *
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约列表
     */
    List<Appointment> getAppointmentsByUserIdAndStatus(Long userId, String status);

    /**
     * 根据房东ID获取收到的预约列表
     *
     * @param landlordId 房东ID
     * @param status     预约状态
     * @return 预约列表
     */
    List<Appointment> getAppointmentsByLandlordId(Long landlordId, String status);

    /**
     * 更新预约状态
     *
     * @param id     预约ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateAppointmentStatus(Long id, String status);

    /**
     * 原子化状态流转：仅在当前状态与期望状态一致时更新，并写入流程轨迹。
     *
     * @param id             预约ID
     * @param expectedStatus 期望的当前状态
     * @param toStatus       目标状态
     * @param action         动作标识
     * @param operatorId     操作人ID
     * @param operatorRole   操作人角色
     * @param remark         审批意见
     * @return 是否流转成功
     */
    boolean transitionStatus(Long id, String expectedStatus, String toStatus, String action,
                             Long operatorId, String operatorRole, String remark);

    /**
     * 记录通知轨迹。
     */
    void recordNotification(Long appointmentId, String status, Long operatorId, String operatorRole, String message);

    /**
     * 写入通知事务 Outbox，由异步处理器投递 WebSocket 消息。
     */
    void enqueueNotification(Long appointmentId, String eventType, String message, Long targetUserId);

    /**
     * 获取预约完整流程时间线。
     */
    List<AppointmentFlow> getFlows(Long appointmentId);

    /**
     * 取消预约
     *
     * @param id 预约ID
     * @return 是否取消成功
     */
    boolean cancelAppointment(Long id);

    /**
     * 删除预约
     *
     * @param id 预约ID
     * @return 是否删除成功
     */
    boolean deleteAppointment(Long id);

    /**
     * 获取所有预约（管理员用）
     *
     * @param status 预约状态筛选
     * @return 所有预约列表
     */
    List<Appointment> getAllAppointments(String status);
}
