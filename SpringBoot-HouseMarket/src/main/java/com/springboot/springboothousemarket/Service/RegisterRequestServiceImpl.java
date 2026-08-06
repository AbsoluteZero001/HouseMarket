package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.RegisterRequestMapper;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RegisterRequestServiceImpl implements RegisterRequestService {

    private static final Set<String> REGISTER_ROLES = Set.of("TENANT", "LANDLORD");

    private final RegisterRequestMapper mapper;
    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;
    private final LandlordApplicationService landlordApplicationService;

    public RegisterRequestServiceImpl(RegisterRequestMapper mapper, UsersService usersService,
                                      PasswordEncoder passwordEncoder,
                                      LandlordApplicationService landlordApplicationService) {
        this.mapper = mapper;
        this.usersService = usersService;
        this.passwordEncoder = passwordEncoder;
        this.landlordApplicationService = landlordApplicationService;
    }

    @Override
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public void register(RegisterRequest user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!REGISTER_ROLES.contains(role)) {
            throw new RuntimeException("仅支持注册租客或房东账号");
        }
        Users exist = usersService.getUserByUsername(user.getUsername());
        if (exist != null) {
            throw new RuntimeException("用户已存在");
        }
        user.setRole(role);
        user.setStatus("normal");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        mapper.register(user);

        if ("LANDLORD".equals(role)) {
            Long userId = user.getId();
            if (userId == null) {
                Users created = usersService.getUserByUsername(user.getUsername());
                userId = created != null ? created.getId() : null;
            }
            if (userId != null) {
                landlordApplicationService.submit(userId, user.getUsername(), null, null);
            }
        }
    }

    @Override
    public RegisterRequest login(String username, String password, String role) {
        RegisterRequest user = mapper.findUserByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getRole().equals(role)) {
            throw new RuntimeException("登录类型错误");
        }
        if (!"normal".equals(user.getStatus())) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        return user;
    }
}
