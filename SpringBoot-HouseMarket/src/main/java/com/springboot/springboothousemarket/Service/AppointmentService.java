package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Appointment;

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
}