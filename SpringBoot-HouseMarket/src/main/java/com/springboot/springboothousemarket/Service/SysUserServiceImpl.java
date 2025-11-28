package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entitiy.SysUser;
import com.springboot.springboothousemarket.Mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

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
        sysUserMapper.update(sysUser);
        return sysUser;
    }

    @Override
    public boolean deleteUser(Long id) {
        return sysUserMapper.deleteById(id) > 0;
    }

    @Override
    public List<SysUser> getAllUsers() {
        return sysUserMapper.selectAll();
    }

    @Override
    public SysUser getUserByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }
}