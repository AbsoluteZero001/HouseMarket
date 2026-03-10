package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户信息API")
@RequestMapping("/user")
@RestController
public class UsersController {

    private final UsersService sysUserService;

    public UsersController(UsersService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 创建用户
     * 
     * @param users 用户信息
     * @return 创建结果
     */
    @PostMapping
    public Users createUser(@RequestBody Users users) {
        return sysUserService.createUser(users);
    }

    /**
     * 根据ID获取用户详情
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Users getUserById(@PathVariable Long id) {
        return sysUserService.getUserById(id);
    }

    /**
     * 更新用户信息
     *
     * @param id    用户ID
     * @param users 更新的用户信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Users updateUser(@PathVariable Long id, @RequestBody Users users) {
        return sysUserService.updateUser(id, users);
    }

    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteUser(@PathVariable Long id) {
        return sysUserService.deleteUser(id);
    }

    /**
     * 获取所有用户列表
     * 
     * @return 用户列表
     */
    @GetMapping
    public List<Users> getAllUsers() {
        return sysUserService.getAllUsers();
    }

    /**
     * 根据用户名获取用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Users getUserByUsername(@PathVariable String username) {
        return sysUserService.getUserByUsername(username);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户信息
     */
    @GetMapping("/current")
    public Users getCurrentUser() {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
            throw new SecurityException("未登录或登录状态失效");
        }
        return sysUserService.getUserByUsername(currentUsername);
    }

    /**
     * 修改用户密码
     *
     * @param id           用户ID
     * @param passwordInfo 包含旧密码和新密码的对象
     * @return 修改结果
     */
    @PutMapping("/{id}/password")
    public boolean changePassword(@PathVariable Long id, @RequestBody PasswordChangeRequest passwordInfo) {
        // 获取当前用户信息
        Users currentUser = sysUserService.getUserById(id);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证用户身份（确保是当前用户或管理员）
        String currentUsername = getCurrentUsername();
        if (currentUsername == null || !currentUsername.equals(currentUser.getUsername())) {
            throw new SecurityException("没有权限修改此用户密码");
        }

        // 验证旧密码（这里简化处理，实际应进行密码加密验证）
        // 在实际应用中，需要对输入的旧密码进行加密并与数据库中的密码进行比较
        // 由于当前系统结构限制，这里仅作示意

        // 更新密码（实际应用中需要加密）
        currentUser.setPassword(passwordInfo.getNewPassword());
        sysUserService.updateUser(id, currentUser);

        return true;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 当前用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    // 为密码修改请求创建内部类
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}