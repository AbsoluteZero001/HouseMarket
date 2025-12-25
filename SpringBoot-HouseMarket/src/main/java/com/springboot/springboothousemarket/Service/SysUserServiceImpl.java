package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.SysUser;
import com.springboot.springboothousemarket.Mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;

    public SysUserServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public SysUser createUser(SysUser sysUser) {
        sysUser.setIsDeleted(0); // 默认未删除
        sysUserMapper.insert(sysUser);
        return sysUser;
    }

    @Override
    public SysUser getUserById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public SysUser updateUser(Long id, SysUser sysUser) {
        sysUser.setId(id);
        sysUserMapper.updateById(sysUser);
        return sysUser;
    }

    @Override
    public boolean deleteUser(Long id) {
        SysUser user = getUserById(id);
        if (user == null) return false;
        user.setIsDeleted(1);
        sysUserMapper.updateById(user);
        return true;
    }

    @Override
    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public List<SysUser> getAllUsers() {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        return sysUserMapper.selectList(queryWrapper);
    }

}