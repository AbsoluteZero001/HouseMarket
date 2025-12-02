package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseMapper extends BaseMapper<House> {

    List<House> selectByLandlordId(Long landlordId);

    List<House> selectByConditions(@Param("keyword") String keyword,
                                   @Param("type") String type,
                                   @Param("minArea") Double minArea,
                                   @Param("maxArea") Double maxArea,
                                   @Param("minPrice") Double minPrice,
                                   @Param("maxPrice") Double maxPrice,
                                   @Param("address") String address);
}