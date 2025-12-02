package com.springboot.springboothousemarket.Mapper;

import com.springboot.springboothousemarket.Entitiy.HouseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseOrderMapper {

    int insert(HouseOrder houseOrder);

    HouseOrder selectById(Long id);

    int update(HouseOrder houseOrder);

    int deleteById(Long id);

    List<HouseOrder> selectAll();

    List<HouseOrder> selectByUserId(Long userId);

    List<HouseOrder> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}