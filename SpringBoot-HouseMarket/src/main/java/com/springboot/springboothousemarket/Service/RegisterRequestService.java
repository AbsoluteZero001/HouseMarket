package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.dto.RegisterRequest;

public interface RegisterRequestService {
    RegisterRequest show();
    void register(RegisterRequest user);
    RegisterRequest login(String username, String password, String role);
}
