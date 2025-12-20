package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.Favorites;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏数据访问接口
 * 继承MyBatis-Plus提供的BaseMapper，获得基本的CRUD操作
 */
@Mapper
public interface FavoritesMapper extends BaseMapper<Favorites> {

    /**
     * 根据用户ID和房源ID删除收藏记录
     *
     * @param userId  用户ID
     * @param houseId 房源ID
     * @return 删除的记录数
     */
    int deleteByUserIdAndHouseId(@Param("userId") Long userId, @Param("houseId") Long houseId);

    /**
     * 根据用户ID查询其所有收藏的房源
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<Favorites> selectByUserId(Long userId);

    /**
     * 根据用户ID和房源ID查询收藏记录
     * @param userId 用户ID
     * @param houseId 房源ID
     * @return 收藏记录实体
     */
    Favorites selectByUserIdAndHouseId(@Param("userId") Long userId, @Param("houseId") Long houseId);
}