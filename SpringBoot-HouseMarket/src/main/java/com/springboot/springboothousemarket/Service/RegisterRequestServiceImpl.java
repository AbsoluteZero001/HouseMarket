package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Mapper.RegisterRequestMapper;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import org.springframework.stereotype.Service;

@Service
public class RegisterRequestServiceImpl implements RegisterRequestService {

    private final RegisterRequestMapper mapper;

    public RegisterRequestServiceImpl(RegisterRequestMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public RegisterRequest show() {
        return mapper.show();
    }

    @Override
    public void register(RegisterRequest user) {
        RegisterRequest exist = mapper.show(); // demo 用 show() 简单判断
        if (exist != null && exist.getUsername().equals(user.getUsername())) {
            throw new RuntimeException("用户已存在");
        }
        mapper.register(user);
    }

    @Override
    public RegisterRequest login(String username, String password, String role) {
        RegisterRequest user = mapper.login(username, password, role);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }
}
