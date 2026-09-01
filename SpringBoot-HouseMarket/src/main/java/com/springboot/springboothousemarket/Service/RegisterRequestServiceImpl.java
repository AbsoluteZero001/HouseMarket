package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.RegisterRequestMapper;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.RegisterRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 注册/登录服务。
 * <p>
 * 注册闭环设计：所有新账号一律以 TENANT 角色起步；
 * 注册时选择"房东"视为同时提交房东入驻申请（同一事务写入 sysuser + landlord_application），
 * 管理员审核通过后由 LandlordApplicationService 将角色升级为 LANDLORD。
 */
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
    @Transactional
    @CacheEvict(cacheNames = "home:stats", allEntries = true)
    public void register(RegisterRequest user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!REGISTER_ROLES.contains(role)) {
            throw new BusinessException("仅支持注册租客或房东账号");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }
        Users exist = usersService.getUserByUsername(user.getUsername());
        if (exist != null) {
            throw new BusinessException("用户已存在");
        }
        user.setRole("TENANT");
        user.setStatus("normal");
        user.setNickname(user.getNickname() == null || user.getNickname().isBlank()
                ? defaultNickname(role) : user.getNickname());
        user.setAvatar("/uploads/avatars/default.png");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        mapper.register(user);

        // 选择"房东"= 注册即提交入驻申请，审核通过后角色才真正升级
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

    private String defaultNickname(String role) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = usersService.lambdaQuery()
                .eq(Users::getRole, role)
                .apply("DATE(register_time) = CURDATE()")
                .count();
        String prefix = "LANDLORD".equals(role) ? "房东" : "租客";
        return prefix + date + (count + 1);
    }

    @Override
    public RegisterRequest login(String username, String password, String role) {
        RegisterRequest user = mapper.findUserByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!"normal".equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 登录不再按请求中的 role 强校验：账号实际角色以数据库为准，
        // 角色会随管理员审核（房东入驻）动态变化，前端以登录响应中的角色跳转
        return user;
    }
}
