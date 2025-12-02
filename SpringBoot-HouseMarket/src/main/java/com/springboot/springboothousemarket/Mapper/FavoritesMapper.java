package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.Favorites;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoritesMapper extends BaseMapper<Favorites> {

    int deleteByUserIdAndHouseId(@Param("userId") Long userId, @Param("houseId") Long houseId);

    List<Favorites> selectByUserId(Long userId);

    Favorites selectByUserIdAndHouseId(@Param("userId") Long userId, @Param("houseId") Long houseId);
}