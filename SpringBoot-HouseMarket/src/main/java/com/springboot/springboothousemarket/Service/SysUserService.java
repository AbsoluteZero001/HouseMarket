package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entitiy.SysUser;

import java.util.List;

public interface SysUserService {

    /**
     * 创建用户
     *
     * @param sysUser 用户信息
     * @return 创建结果
     */
    SysUser createUser(SysUser sysUser);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser getUserById(Long id);

    /**
     * 更新用户信息
     *
     * @param id      用户ID
     * @param sysUser 更新的用户信息
     * @return 更新结果
     */
    SysUser updateUser(Long id, SysUser sysUser);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    boolean deleteUser(Long id);

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    List<SysUser> getAllUsers();

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserByUsername(String username);
}