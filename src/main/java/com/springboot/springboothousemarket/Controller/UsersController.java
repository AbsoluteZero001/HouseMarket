package com.springboot.springboothousemarket.Controller;

import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Service.UsersService;
import com.springboot.springboothousemarket.common.UserStatus;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.ResponseResult;
import com.springboot.springboothousemarket.dto.UserVO;
import com.springboot.springboothousemarket.Util.ImageUploadValidator;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "用户信息API")
@RequestMapping("/user")
@RestController
public class UsersController {

    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "LANDLORD", "TENANT");

    /** 头像大小上限：2MB。 */
    private static final long AVATAR_MAX_BYTES = 2L * 1024 * 1024;

    /** 昵称长度上限：与 sysuser.nickname 列宽（VARCHAR(100)）保持一致。 */
    private static final int NICKNAME_MAX_LENGTH = 100;

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
     *
     * <p>旧实现没有长度校验：昵称超过 {@code sysuser.nickname} 的列宽（VARCHAR(100)）时，
     * MySQL 直接报错 → {@code DataIntegrityViolationException} →
     * 全局异常处理器按「编程错误」返回 500 与「系统繁忙」，
     * 用户完全不知道自己哪里填错了。这里补上前置校验，返回可操作的 400 文案。
     */
    @PutMapping("/profile")
    public ResponseResult updateNickname(@RequestBody Map<String, String> body,
                                         @AuthenticationPrincipal Users currentUser) {
        if (currentUser == null) {
            throw new SecurityException("请先登录");
        }
        if (body == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String nickname = body.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException("昵称不能为空");
        }
        String trimmed = nickname.trim();
        if (trimmed.length() > NICKNAME_MAX_LENGTH) {
            throw new BusinessException("昵称不能超过 " + NICKNAME_MAX_LENGTH + " 个字符");
        }
        Users user = sysUserService.getUserById(currentUser.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setNickname(trimmed);
        sysUserService.updateUser(user.getId(), user);
        return ResponseResult.ok(null, Map.of("user", UserVO.from(sysUserService.getUserById(user.getId()))));
    }

    /**
     * 上传头像：二进制落盘到 uploadDir/avatars/，数据库只保存图片 URL。
     * <p>
     * 旧实现把 Base64 写进 sysuser.avatar_base64（MEDIUMTEXT），并随登录/列表等接口
     * 全量下发，导致单次响应可达数 MB；现改为「文件 + URL」，
     * 由 WebMvcConfig 的 /uploads/** 静态映射直接提供图片。
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult updateAvatar(
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal Users currentUser) throws IOException {
        MultipartFile upload = avatar != null && !avatar.isEmpty() ? avatar : file;
        // 统一按文件真实字节（magic number）判定类型并限制大小：
        // 不再信任用户提供的文件名与 Content-Type，也不再依赖「后缀合法就放行」的兜底分支
        String extension = ImageUploadValidator.validateAndDetectExtension(upload, AVATAR_MAX_BYTES, "头像");
        byte[] bytes = upload.getBytes();
        // 文件名由服务端随机生成：杜绝用户可控文件名带来的路径穿越与同名覆盖
        String fileName = ImageUploadValidator.randomFileName(extension);
        Path avatarDir = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
        Files.createDirectories(avatarDir);
        Path target = avatarDir.resolve(fileName).normalize();
        if (!target.startsWith(avatarDir)) {
            throw new BusinessException("头像保存路径非法");
        }
        Files.write(target, bytes);

        Users user = sysUserService.getUserById(currentUser.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setAvatar("/uploads/avatars/" + fileName);
        sysUserService.updateUser(user.getId(), user);
        // 清理历史 Base64 大字段（头像已落盘，无需再保留内联副本）
        sysUserService.clearAvatarBase64(user.getId());
        return ResponseResult.ok(null, Map.of("user", UserVO.from(sysUserService.getUserById(user.getId()))));
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
