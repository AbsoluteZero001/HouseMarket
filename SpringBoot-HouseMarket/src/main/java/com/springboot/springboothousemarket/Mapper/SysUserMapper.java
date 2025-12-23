package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户数据访问接口
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 使用@Mapper注解标记为MyBatis的Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 根据用户名查询系统用户
     *
     * @param username 用户名
     * @return 匹配的用户对象，如果未找到则返回null
     */
    SysUser selectByUsername(String username);
}