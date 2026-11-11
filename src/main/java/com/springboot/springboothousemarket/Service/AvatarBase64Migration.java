package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entity.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 存量头像数据迁移（幂等，一次性）：
 * <p>
 * 早期版本把头像以 Base64 内联写入 sysuser.avatar_base64（MEDIUMTEXT）。
 * 现改为「图片落盘 + 只存 URL」，且 {@code Users.avatarBase64} 已标记 {@code @JsonIgnore}，
 * 因此必须把存量 Base64 落盘并回填 avatar，否则老用户上传过的头像会直接消失。
 * <p>
 * 迁移完成后 avatar_base64 全部为 NULL，后续启动查询即为空，零开销。
 * 可通过 {@code app.avatar.migrate-base64=false} 关闭。
 */
@Slf4j
@Component
public class AvatarBase64Migration implements ApplicationRunner {

    private final UsersService usersService;

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.avatar.migrate-base64:true}")
    private boolean enabled;

    public AvatarBase64Migration(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        List<Users> legacyUsers;
        try {
            legacyUsers = usersService.lambdaQuery()
                    .isNotNull(Users::getAvatarBase64)
                    .ne(Users::getAvatarBase64, "")
                    .list();
        } catch (Exception e) {
            log.warn("存量头像迁移跳过，查询失败: {}", e.getMessage());
            return;
        }
        if (legacyUsers.isEmpty()) {
            return;
        }
        int migrated = 0;
        for (Users user : legacyUsers) {
            try {
                byte[] bytes = decodeDataUrl(user.getAvatarBase64());
                if (bytes == null || bytes.length == 0) {
                    continue;
                }
                String extension = detectExtension(bytes);
                if (extension == null) {
                    extension = ".png";
                }
                Path avatarDir = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
                Files.createDirectories(avatarDir);
                String fileName = "mig_" + user.getId() + "_"
                        + UUID.randomUUID().toString().replace("-", "") + extension;
                Path target = avatarDir.resolve(fileName).normalize();
                if (!target.startsWith(avatarDir)) {
                    continue;
                }
                Files.write(target, bytes);
                Users update = new Users();
                update.setId(user.getId());
                update.setAvatar("/uploads/avatars/" + fileName);
                usersService.updateById(update);
                usersService.clearAvatarBase64(user.getId());
                migrated++;
            } catch (Exception e) {
                // 单个用户迁移失败不影响整体，也绝不阻断应用启动
                log.warn("存量头像迁移失败, userId={}, reason={}", user.getId(), e.getMessage());
            }
        }
        log.info("存量 Base64 头像迁移完成, 迁移数量={}, 待处理={}", migrated, legacyUsers.size());
    }

    private byte[] decodeDataUrl(String dataUrl) {
        if (dataUrl == null || dataUrl.isBlank()) {
            return null;
        }
        int comma = dataUrl.indexOf(',');
        String payload = comma >= 0 ? dataUrl.substring(comma + 1) : dataUrl;
        try {
            return Base64.getDecoder().decode(payload.replaceAll("\\s", ""));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String detectExtension(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return ".png";
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return ".jpg";
        }
        if (bytes.length >= 3 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
            return ".gif";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return ".webp";
        }
        return null;
    }
}