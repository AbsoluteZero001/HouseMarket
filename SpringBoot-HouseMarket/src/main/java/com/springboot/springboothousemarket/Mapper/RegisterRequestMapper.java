package com.springboot.springboothousemarket.Mapper;

import com.springboot.springboothousemarket.dto.RegisterRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RegisterRequestMapper {

    RegisterRequest show();

    int register(RegisterRequest request);

    RegisterRequest login(String username, String password, String role);
}
