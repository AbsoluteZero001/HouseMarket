package com.springboot.springboothousemarket.Mapper;

import com.springboot.springboothousemarket.Entitiy.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseMapper {

    int insert(House house);

    House selectById(Long id);

    int update(House house);

    int deleteById(Long id);

    List<House> selectAll();

    List<House> selectByLandlordId(Long landlordId);

    List<House> selectByConditions(@Param("keyword") String keyword,
                                   @Param("type") String type,
                                   @Param("minArea") Double minArea,
                                   @Param("maxArea") Double maxArea,
                                   @Param("minPrice") Double minPrice,
                                   @Param("maxPrice") Double maxPrice,
                                   @Param("address") String address);
}