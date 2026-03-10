package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.UsersMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    private final UsersMapper usersMapper;

    public UsersServiceImpl(UsersMapper usersMapper) {
        this.usersMapper = usersMapper;
    }

    @Override
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
    public Users updateUser(Long id, Users users) {
        users.setId(id);
        usersMapper.updateById(users);
        return users;
    }

    @Override
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

}