package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.UsersMapper;
import com.springboot.springboothousemarket.common.UserStatus;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.UserVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        // isDeleted 带 @TableLogic：逻辑删除字段不能经 updateById 设置（会被排除出 SET 子句），
        // 必须走 deleteById，由 MyBatis-Plus 转写为 UPDATE is_deleted=1
        return usersMapper.deleteById(id) > 0;
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
}
