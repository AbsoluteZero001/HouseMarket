package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.UsersMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    private final UsersMapper usersMapper;

    public UsersServiceImpl(UsersMapper usersMapper) {
        this.usersMapper = usersMapper;
    }

    @Override
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public Users createUser(Users users) {
        users.setIsDeleted(0); // 默认未删除
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
        usersMapper.updateById(users);
        return users;
    }

    @Override
    public boolean updatePassword(Long id, String encodedPassword) {
        Users user = new Users();
        user.setId(id);
        user.setPassword(encodedPassword);
        return usersMapper.updateById(user) > 0;
    }

    @Override
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public boolean deleteUser(Long id) {
        Users user = getUserById(id);
        if (user == null)
            return false;
        user.setIsDeleted(1);
        usersMapper.updateById(user);
        return true;
    }

    @Override
    public Users getUserByUsername(String username) {
        return usersMapper.selectByUsername(username);
    }

    @Override
    public List<Users> getAllUsers() {
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        return usersMapper.selectList(queryWrapper);
    }

    @Override
    public Users verifyLandlord(Long userId, String nickname, String realName, String idCardNo) {
        Users user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!"LANDLORD".equals(user.getRole())) {
            throw new RuntimeException("只有房东可以进行实名认证");
        }
        if (idCardNo == null || !idCardNo.matches("\\d{17}[\\dXx]")) {
            throw new RuntimeException("请输入18位有效身份证号");
        }
        if (realName == null || realName.isBlank()) {
            throw new RuntimeException("请输入真实姓名");
        }

        user.setNickname(nickname == null || nickname.isBlank() ? user.getUsername() : nickname);
        user.setRealName(realName);
        user.setIdCardNo(idCardNo);
        user.setRealNameVerified(1);
        user.setVerifiedTime(LocalDateTime.now());
        updateById(user);
        return user;
    }

}
