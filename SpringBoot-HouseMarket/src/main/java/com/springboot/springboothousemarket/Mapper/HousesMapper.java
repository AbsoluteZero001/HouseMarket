package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.springboot.springboothousemarket.Entity.Houses;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房源数据访问层（Mapper）
 * 基于 MyBatis-Plus
 */
@Mapper
public interface HousesMapper extends BaseMapper<Houses> {

    /**
     * 根据房东 ID 查询房源列表
     *
     * @param landlordId 房东 ID
     * @return 房源列表
     */
    List<Houses> selectByLandlordId(@Param("landlordId") Long landlordId);

    /**
     * 房源分页 + 条件查询
     *
     * @param page 分页对象（MyBatis-Plus）
     * @param keyword 关键词（标题 / 描述）
     * @param type 房屋类型
     * @param minArea 最小面积
     * @param maxArea 最大面积
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @param address 地址
     * @return 分页房源数据
     */
    Page<Houses> selectPageByConditions(
            Page<Houses> page,
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("minArea") Double minArea,
            @Param("maxArea") Double maxArea,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("address") String address
    );
}
