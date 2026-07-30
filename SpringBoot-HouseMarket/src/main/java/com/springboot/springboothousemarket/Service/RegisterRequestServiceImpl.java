package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.RegisterRequestMapper;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class RegisterRequestServiceImpl implements RegisterRequestService {

    private final RegisterRequestMapper mapper;
    private final UsersService usersService;

    public RegisterRequestServiceImpl(RegisterRequestMapper mapper, UsersService usersService) {
        this.mapper = mapper;
        this.usersService = usersService;
    }

    @Override
    public void register(RegisterRequest user) {
        Users exist = usersService.getUserByUsername(user.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户已存在");
        }
        mapper.register(user);
    }

    @Override
    public RegisterRequest login(String username, String password, String role) {
        RegisterRequest user = mapper.findUserByUsernameAndPassword(username, password);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getRole().equals(role)) {
            throw new RuntimeException("登录类型错误");
        }

        return user;
    }
}
