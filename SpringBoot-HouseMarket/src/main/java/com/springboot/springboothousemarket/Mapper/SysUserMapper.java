package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    default SysUser selectByUsername(String username) {
        return selectOne(new QueryWrapper<SysUser>().eq("username", username));
    }
}