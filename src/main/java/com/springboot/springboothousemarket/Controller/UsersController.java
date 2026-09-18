package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.common.UserStatus;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.ResponseResult;
import com.springboot.springboothousemarket.dto.UserVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "用户信息API")
@RequestMapping("/user")
@RestController
public class UsersController {

    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "LANDLORD", "TENANT");

    private final UsersService sysUserService;
    private final PasswordEncoder passwordEncoder;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    public UsersController(UsersService sysUserService, PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建用户（管理员）。
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult createUser(@RequestBody Users users) {
        if (users.getRole() != null && !VALID_ROLES.contains(users.getRole())) {
            throw new BusinessException("非法的用户角色");
        }
        encodePasswordIfNeeded(users);
        Users created = sysUserService.createUser(users);
        return ResponseResult.ok("用户已创建", Map.of("user", UserVO.from(created)));
    }

    /**
     * 根据ID获取用户详情（管理员，脱敏 VO）。
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult getUserById(@PathVariable Long id) {
        Users user = sysUserService.getUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return ResponseResult.ok(null, Map.of("user", UserVO.from(user)));
    }

    /**
     * 更新用户基础信息（管理员）。
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult updateUser(@PathVariable Long id, @RequestBody Users users) {
        encodePasswordIfNeeded(users);
        Users updated = sysUserService.updateUser(id, users);
        return ResponseResult.ok("用户已更新", Map.of("user", UserVO.from(updated)));
    }

    /**
     * 删除用户（管理员，含自我保护与级联清理）。
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult deleteUser(@PathVariable Long id, @AuthenticationPrincipal Users currentUser) {
        boolean result = sysUserService.deleteUser(id, currentUser);
        return result ? ResponseResult.ok("用户及其关联数据已删除") : ResponseResult.fail("用户删除失败");
    }

    /**
     * 用户列表（管理员，脱敏 VO：不含密码哈希/身份证号）。
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult getAllUsers() {
        List<UserVO> users = sysUserService.getAllUsersVO();
        return ResponseResult.ok(null, Map.of("users", users));
    }

    /**
     * 启用/禁用用户（管理员）。被禁用账号立即失去业务权限（JwtFilter 每次请求校验账号状态）。
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseResult changeStatus(@PathVariable Long id,
                                       @RequestBody Map<String, String> body,
                                       @AuthenticationPrincipal Users currentUser) {
        String status = body == null ? null : body.get("status");
        boolean result = sysUserService.changeStatus(id, status, currentUser);
        return result ? ResponseResult.ok(UserStatus.DISABLED.equals(status) ? "账号已禁用" : "账号已启用")
                : ResponseResult.fail("状态更新失败");
    }

    /**
     * 更新当前用户昵称。
     */
    @PutMapping("/profile")
    public ResponseResult updateNickname(@RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal Users currentUser) {
        Users user = sysUserService.getUserById(currentUser.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String nickname = body.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException("昵称不能为空");
        }
        user.setNickname(nickname.trim());
        sysUserService.updateUser(user.getId(), user);
        return ResponseResult.ok(null, Map.of("user", sysUserService.getUserById(user.getId())));
    }

    /**
     * 上传头像（Base64 入库）。
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult updateAvatar(
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal Users currentUser) throws IOException {
        MultipartFile upload = avatar != null && !avatar.isEmpty() ? avatar : file;
        if (upload == null || upload.isEmpty()) {
            throw new BusinessException("上传头像不能为空");
        }
        byte[] bytes = upload.getBytes();
        if (bytes.length > 2 * 1024 * 1024) {
            throw new BusinessException("头像大小不能超过 2MB");
        }
        String extension = "";
        String original = upload.getOriginalFilename();
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                extension = original.substring(dot);
            }
        }
        if (!extension.isBlank()
                && !Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(extension.toLowerCase())) {
            throw new BusinessException("头像仅支持 JPG/PNG/GIF/WEBP 格式");
        }
        String mime = upload.getContentType();
        if (mime == null || mime.isBlank() || "application/octet-stream".equalsIgnoreCase(mime)) {
            mime = extension.equalsIgnoreCase(".png") ? "image/png"
                    : extension.equalsIgnoreCase(".webp") ? "image/webp"
                      : extension.equalsIgnoreCase(".gif") ? "image/gif"
                        : "image/jpeg";
        }
        String dataUrl = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);

        Users user = sysUserService.getUserById(currentUser.getId());
        user.setAvatarBase64(dataUrl);
        sysUserService.updateUser(user.getId(), user);
        return ResponseResult.ok(null, Map.of("user", sysUserService.getUserById(user.getId())));
    }

    /**
     * 修改密码：校验旧密码 → 更新新密码。仅本人可操作。
     */
    @PutMapping("/{id}/password")
    public ResponseResult changePassword(@PathVariable Long id,
                                         @RequestBody PasswordChangeRequest passwordInfo,
                                         @AuthenticationPrincipal Users currentUser) {
        if (currentUser == null || !currentUser.getId().equals(id)) {
            throw new BusinessException("只能修改自己的密码");
        }
        Users user = sysUserService.getUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (passwordInfo.getOldPassword() == null || passwordInfo.getOldPassword().isEmpty()) {
            throw new BusinessException("请输入旧密码");
        }
        if (!passwordEncoder.matches(passwordInfo.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (passwordInfo.getNewPassword() == null || passwordInfo.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        if (passwordInfo.getNewPassword().length() > 64) {
            throw new BusinessException("新密码长度不能超过64位");
        }
        if (passwordEncoder.matches(passwordInfo.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        sysUserService.updatePassword(id, passwordEncoder.encode(passwordInfo.getNewPassword()));
        return ResponseResult.ok("密码修改成功，请牢记新密码");
    }

    private void encodePasswordIfNeeded(Users users) {
        String password = users.getPassword();
        if (password != null && !password.isEmpty() && !password.startsWith("$2")) {
            users.setPassword(passwordEncoder.encode(password));
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * 密码修改请求体。
     */
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
