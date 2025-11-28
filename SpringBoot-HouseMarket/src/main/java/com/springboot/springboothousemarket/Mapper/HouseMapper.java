package com.springboot.springboothousemarket.Mapper;

import com.springboot.springboothousemarket.Entitiy.House;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HouseMapper {

    @Insert("INSERT INTO house(landlord_id, title, description, address, price, house_type, images, status, is_deleted) " +
            "VALUES(#{landlordId}, #{title}, #{description}, #{address}, #{price}, #{houseType}, #{images}, #{status}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(House house);

    @Select("SELECT * FROM house WHERE id = #{id} AND is_deleted = 0")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "landlordId", column = "landlord_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "description", column = "description"),
            @Result(property = "address", column = "address"),
            @Result(property = "price", column = "price"),
            @Result(property = "houseType", column = "house_type"),
            @Result(property = "images", column = "images"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    House selectById(Long id);

    @Update("UPDATE house SET landlord_id=#{landlordId}, title=#{title}, description=#{description}, address=#{address}, " +
            "price=#{price}, house_type=#{houseType}, images=#{images}, status=#{status}, is_deleted=#{isDeleted}, " +
            "update_time=#{updateTime} WHERE id = #{id}")
    int update(House house);

    @Update("UPDATE house SET is_deleted = 1 WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM house WHERE is_deleted = 0")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "landlordId", column = "landlord_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "description", column = "description"),
            @Result(property = "address", column = "address"),
            @Result(property = "price", column = "price"),
            @Result(property = "houseType", column = "house_type"),
            @Result(property = "images", column = "images"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    List<House> selectAll();

    @Select("SELECT * FROM house WHERE landlord_id = #{landlordId} AND is_deleted = 0")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "landlordId", column = "landlord_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "description", column = "description"),
            @Result(property = "address", column = "address"),
            @Result(property = "price", column = "price"),
            @Result(property = "houseType", column = "house_type"),
            @Result(property = "images", column = "images"),
            @Result(property = "status", column = "status"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    List<House> selectByLandlordId(Long landlordId);
}