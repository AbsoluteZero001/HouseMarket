package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.SysUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户信息API")
@RequestMapping("/user")
@RestController
public class UsersController {

    private final SysUserService sysUserService;

    public UsersController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 创建用户
     * @param users 用户信息
     * @return 创建结果
     */
    @PostMapping
    public Users createUser(@RequestBody Users users) {
        return sysUserService.createUser(users);
    }

    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Users getUserById(@PathVariable Long id) {
        return sysUserService.getUserById(id);
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param users 更新的用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Users updateUser(@PathVariable Long id, @RequestBody Users users) {
        return sysUserService.updateUser(id, users);
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable Long id) {
        return sysUserService.deleteUser(id);
    }

    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    @GetMapping
    public List<Users> getAllUsers() {
        return sysUserService.getAllUsers();
    }

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Users getUserByUsername(@PathVariable String username) {
        return sysUserService.getUserByUsername(username);
    }
}