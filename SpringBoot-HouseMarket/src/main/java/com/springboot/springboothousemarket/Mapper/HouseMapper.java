package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房屋数据访问接口
 * 继承了MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {

    /**
     * 根据房东ID查询房屋列表
     *
     * @param landlordId 房东ID
     * @return 房屋列表
     */
    List<House> selectByLandlordId(Long landlordId);

    /**
     * 根据条件查询房屋列表
     * @param keyword 关键词搜索
     * @param房屋类型
     * @param minArea 最小面积
     * @param maxArea 最大面积
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param address 地址
     * @return 符合条件的房屋列表
     */
    List<House> selectByConditions(@Param("keyword") String keyword,
                                   @Param("type") String type,
                                   @Param("minArea") Double minArea,
                                   @Param("maxArea") Double maxArea,
                                   @Param("minPrice") Double minPrice,
                                   @Param("maxPrice") Double maxPrice,
                                   @Param("address") String address);
}