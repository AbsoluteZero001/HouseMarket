package com.springboot.springboothousemarket.Mapper;

import com.springboot.springboothousemarket.Entitiy.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysUserMapper {

    @Insert("INSERT INTO sys_user(username, password, real_name, role, phone, avatar, is_deleted) " +
            "VALUES(#{username}, #{password}, #{realName}, #{role}, #{phone}, #{avatar}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser sysUser);

    @Select("SELECT * FROM sys_user WHERE id = #{id} AND is_deleted = 0")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "role", column = "role"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    SysUser selectById(Long id);

    @Update("UPDATE sys_user SET username=#{username}, password=#{password}, real_name=#{realName}, role=#{role}, " +
            "phone=#{phone}, avatar=#{avatar}, is_deleted=#{isDeleted}, update_time=#{updateTime} WHERE id = #{id}")
    int update(SysUser sysUser);

    @Update("UPDATE sys_user SET is_deleted = 1 WHERE id = #{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM sys_user WHERE is_deleted = 0")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "role", column = "role"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    List<SysUser> selectAll();

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "role", column = "role"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "isDeleted", column = "is_deleted")
    })
    SysUser selectByUsername(String username);
}