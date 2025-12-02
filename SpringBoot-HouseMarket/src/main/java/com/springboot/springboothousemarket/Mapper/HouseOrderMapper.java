package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.HouseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseOrderMapper extends BaseMapper<HouseOrder> {

    List<HouseOrder> selectByUserId(Long userId);

    List<HouseOrder> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}