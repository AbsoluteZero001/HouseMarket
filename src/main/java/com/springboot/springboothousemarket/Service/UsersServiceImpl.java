package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.UsersMapper;
import com.springboot.springboothousemarket.common.UserStatus;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.UserVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    private final UsersMapper usersMapper;
    private final UserCleanupService userCleanupService;

    public UsersServiceImpl(UsersMapper usersMapper, UserCleanupService userCleanupService) {
        this.usersMapper = usersMapper;
        this.userCleanupService = userCleanupService;
    }

    @Override
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public Users createUser(Users users) {
        users.setIsDeleted(0);
        if (users.getStatus() == null || users.getStatus().isBlank()) {
            users.setStatus(UserStatus.NORMAL);
        }
        usersMapper.insert(users);
        return users;
    }

    @Override
    public Users getUserById(Long id) {
        return usersMapper.selectById(id);
    }

    @Override
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public Users updateUser(Long id, Users users) {
        users.setId(id);
        // 不允许通过通用更新通道变更角色与状态，二者各有独立受控入口
        users.setRole(null);
        users.setStatus(null);
        usersMapper.updateById(users);
        return getUserById(id);
    }

    @Override
    public boolean updatePassword(Long id, String encodedPassword) {
        Users user = new Users();
        user.setId(id);
        user.setPassword(encodedPassword);
        return usersMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public boolean deleteUser(Long id, Users operator) {
        if (operator != null && operator.getId().equals(id)) {
            throw new BusinessException("不能删除当前登录的管理员账号");
        }
        Users user = getUserById(id);
        if (user == null) {
            return false;
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("管理员账号不允许删除");
        }
        // 级联清理房源/预约/收藏/聊天/通知/申请单，避免孤儿数据
        userCleanupService.cleanupUserData(id);
        // 释放用户名：必须先改名，再做逻辑删除
        releaseUsername(user);
        // isDeleted 带 @TableLogic：逻辑删除字段不能经 updateById 设置（会被排除出 SET 子句），
        // 必须走 deleteById，由 MyBatis-Plus 转写为 UPDATE is_deleted=1
        return usersMapper.deleteById(id) > 0;
    }

    /**
     * 逻辑删除前释放「用户名」这一唯一键占用。
     *
     * <p>背景：{@code sysuser.uk_username} 是 {@code username} 单列唯一索引，
     * 而逻辑删除（{@code is_deleted=1}）的行仍然留在表里、依旧占用该用户名；
     * 同时所有按用户名查询的地方都带 {@code AND is_deleted = 0}，
     * 于是「注册前的重名检查」查不到已删除用户 → 直接 INSERT 撞唯一键，
     * 表现为 HTTP 500 DuplicateKeyException。
     * 结果就是：管理员删掉某个用户后，该用户名<b>永远</b>无法再次注册。
     *
     * <p>解决办法：删除前把用户名改写成「原名#del#&lt;用户ID&gt;」，
     * 既保留审计线索（还能看出原用户名），又腾出原用户名供重新注册。
     */
    private void releaseUsername(Users user) {
        String original = user.getUsername();
        if (original == null || original.isBlank()) {
            return;
        }
        String suffix = "#del#" + user.getId();
        // sysuser.username 为 VARCHAR(100)，改名后总长不得超列宽
        int keep = Math.max(1, 100 - suffix.length());
        String released = original.substring(0, Math.min(original.length(), keep)) + suffix;
        Users update = new Users();
        update.setId(user.getId());
        update.setUsername(released);
        try {
            usersMapper.updateById(update);
        } catch (DuplicateKeyException conflict) {
            // 极端情况：改名后的用户名恰好被占用（例如有人注册了带 #del# 的用户名），
            // 退化为 UUID 命名，保证删除流程不因唯一键冲突而整体失败。
            Users fallback = new Users();
            fallback.setId(user.getId());
            fallback.setUsername("deleted_" + user.getId() + "_"
                    + UUID.randomUUID().toString().replace("-", ""));
            usersMapper.updateById(fallback);
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public boolean changeStatus(Long id, String status, Users operator) {
        if (!UserStatus.isValid(status)) {
            throw new BusinessException("非法的用户状态");
        }
        if (operator != null && operator.getId().equals(id)) {
            throw new BusinessException("不能变更当前登录账号的状态");
        }
        Users user = getUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("管理员账号不允许禁用");
        }
        Users update = new Users();
        update.setId(id);
        update.setStatus(status);
        return usersMapper.updateById(update) > 0;
    }

    @Override
    public List<UserVO> getAllUsersVO() {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0).orderByDesc("register_time");
        return usersMapper.selectList(queryWrapper).stream().map(UserVO::from).toList();
    }

    @Override
    public Users getUserByUsername(String username) {
        return usersMapper.selectByUsername(username);
    }

    @Override
    public boolean clearAvatarBase64(Long id) {
        if (id == null) {
            return false;
        }
        return lambdaUpdate()
                .eq(Users::getId, id)
                .set(Users::getAvatarBase64, null)
                .update();
    }
}
