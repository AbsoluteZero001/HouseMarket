package com.springboot.springboothousemarket.dto;

import com.springboot.springboothousemarket.Entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户安全视图对象：明确控制对外返回字段。
 * 密码哈希、身份证号、头像 Base64 大字段一律不外泄。
 */
@Data
@Schema(description = "用户信息（脱敏）")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色: ADMIN/LANDLORD/TENANT")
    private String role;

    @Schema(description = "状态: normal/disabled")
    private String status;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "实名认证状态: 0未实名, 1已实名")
    private Integer realNameVerified;

    @Schema(description = "注册时间")
    private Object createTime;

    public static UserVO from(Users user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRealNameVerified(user.getRealNameVerified());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
