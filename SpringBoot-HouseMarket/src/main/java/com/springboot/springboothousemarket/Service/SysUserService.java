package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Users;

import java.util.List;

public interface SysUserService extends IService<Users> {

    /**
     * 创建用户
     *
     * @param users 用户信息
     * @return 创建结果
     */
    Users createUser(Users users);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Users getUserById(Long id);

    /**
     * 更新用户信息
     *
     * @param id      用户ID
     * @param users 更新的用户信息
     * @return 更新结果
     */
    Users updateUser(Long id, Users users);

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
    List<Users> getAllUsers();

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Users getUserByUsername(String username);
}