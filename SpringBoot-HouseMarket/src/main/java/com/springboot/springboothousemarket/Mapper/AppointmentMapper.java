package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.Appointment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约映射器接口
 * 继承自MyBatis-Plus提供的BaseMapper接口，提供了基础的CRUD操作
 * 使用@Mapper注解标记为MyBatis的映射器接口
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    // 该接口无需额外定义方法，直接继承BaseMapper中的方法即可满足需求
    // BaseMapper已提供了对Appointment实体类的完整数据库操作方法
}