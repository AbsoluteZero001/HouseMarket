package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.SysUser;
import com.springboot.springboothousemarket.Service.SysUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户信息API")
@RequestMapping("/user")
@RestController
public class SysUserController {

    private final SysUserService sysUserService;
    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 创建用户
     * @param sysUser 用户信息
     * @return 创建结果
     */
    @PostMapping
    public SysUser createUser(@RequestBody SysUser sysUser) {
        return sysUserService.createUser(sysUser);
    }

    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public SysUser getUserById(@PathVariable Long id) {
        return sysUserService.getUserById(id);
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param sysUser 更新的用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public SysUser updateUser(@PathVariable Long id, @RequestBody SysUser sysUser) {
        return sysUserService.updateUser(id, sysUser);
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
    public List<SysUser> getAllUsers() {
        return sysUserService.getAllUsers();
    }

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public SysUser getUserByUsername(@PathVariable String username) {
        return sysUserService.getUserByUsername(username);
    }
}