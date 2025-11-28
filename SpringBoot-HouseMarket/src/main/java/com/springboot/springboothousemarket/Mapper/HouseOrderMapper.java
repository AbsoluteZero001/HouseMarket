package com.springboot.springboothousemarket.Mapper;

import com.springboot.springboothousemarket.Entitiy.HouseOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HouseOrderMapper {

    @Insert("INSERT INTO house_order(house_id, tenant_id, landlord_id, price, status, start_date, end_date) " +
            "VALUES(#{houseId}, #{tenantId}, #{landlordId}, #{price}, #{status}, #{startDate}, #{endDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HouseOrder houseOrder);

    @Select("SELECT * FROM house_order WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "houseId", column = "house_id"),
            @Result(property = "tenantId", column = "tenant_id"),
            @Result(property = "landlordId", column = "landlord_id"),
            @Result(property = "price", column = "price"),
            @Result(property = "status", column = "status"),
            @Result(property = "startDate", column = "start_date"),
            @Result(property = "endDate", column = "end_date"),
            @Result(property = "createTime", column = "create_time")
    })
    HouseOrder selectById(Long id);

    @Update("UPDATE house_order SET house_id=#{houseId}, tenant_id=#{tenantId}, landlord_id=#{landlordId}, " +
            "price=#{price}, status=#{status}, start_date=#{startDate}, end_date=#{endDate} WHERE id = #{id}")
    int update(HouseOrder houseOrder);

    @Delete("DELETE FROM house_order WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM house_order")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "houseId", column = "house_id"),
            @Result(property = "tenantId", column = "tenant_id"),
            @Result(property = "landlordId", column = "landlord_id"),
            @Result(property = "price", column = "price"),
            @Result(property = "status", column = "status"),
            @Result(property = "startDate", column = "start_date"),
            @Result(property = "endDate", column = "end_date"),
            @Result(property = "createTime", column = "create_time")
    })
    List<HouseOrder> selectAll();

    @Select("SELECT * FROM house_order WHERE tenant_id = #{userId} OR landlord_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "houseId", column = "house_id"),
            @Result(property = "tenantId", column = "tenant_id"),
            @Result(property = "landlordId", column = "landlord_id"),
            @Result(property = "price", column = "price"),
            @Result(property = "status", column = "status"),
            @Result(property = "startDate", column = "start_date"),
            @Result(property = "endDate", column = "end_date"),
            @Result(property = "createTime", column = "create_time")
    })
    List<HouseOrder> selectByUserId(Long userId);
}