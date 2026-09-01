package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.dto.UserVO;

import java.util.List;

public interface UsersService extends IService<Users> {

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
     * @param id    用户ID
     * @param users 更新的用户信息
     * @return 更新结果
     */
    Users updateUser(Long id, Users users);

    /**
     * 直接更新已加密的密码
     *
     * @param id              用户ID
     * @param encodedPassword BCrypt密码
     * @return 是否更新成功
     */
    boolean updatePassword(Long id, String encodedPassword);

    /**
     * 删除用户（含关联数据级联清理），含自我保护与管理员保护校验。
     *
     * @param id       目标用户ID
     * @param operator 执行删除的管理员
     * @return 是否删除成功
     */
    boolean deleteUser(Long id, Users operator);

    /**
     * 获取所有用户列表（脱敏 VO，不含密码哈希/身份证号）。
     *
     * @return 用户列表
     */
    List<UserVO> getAllUsersVO();

    /**
     * 管理员启用/禁用用户账号。
     *
     * @param id       目标用户ID
     * @param status   normal/disabled
     * @param operator 执行操作的管理员
     * @return 是否更新成功
     */
    boolean changeStatus(Long id, String status, Users operator);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Users getUserByUsername(String username);
}
