package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预约映射器接口
 * 继承自MyBatis-Plus提供的BaseMapper接口，提供了基础的CRUD操作
 * 使用@Mapper注解标记为MyBatis的映射器接口
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 根据用户ID和状态获取预约记录，带关联信息
     *
     * @param userId 用户ID
     * @param status 预约状态
     * @return 预约记录列表
     */
    List<Appointment> selectAppointmentsWithDetails(@Param("userId") Long userId, @Param("status") String status);
}