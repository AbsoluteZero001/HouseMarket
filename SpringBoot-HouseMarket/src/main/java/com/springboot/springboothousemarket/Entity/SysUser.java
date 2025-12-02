package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息")
@TableName("users")
public class SysUser {
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码(加密存储)")
    @TableField("password")
    private String password;

    @Schema(description = "真实姓名")
    @TableField("realName")
    private String realName;

    @Schema(description = "角色: ADMIN(管理员), LANDLORD(房东), TENANT(租户)")
    @TableField("role")
    private String role;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "头像URL")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "用户状态: normal(正常), disabled(禁用)")
    @TableField("status")
    private String status;

    @Schema(description = "注册时间")
    @TableField("registerTime")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("updateTime")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除: 0未删, 1已删")
    @TableLogic
    @TableField("isDeleted")
    private Integer isDeleted;
}
