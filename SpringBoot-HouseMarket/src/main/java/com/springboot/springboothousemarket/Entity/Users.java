package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于存储用户相关信息，包括基本信息、角色状态等
 * 使用MyBatis-Plus和Swagger注解来增强ORM功能和API文档生成
 */
@Data
@Schema(description = "用户信息")
@TableName("users")
public class Users {
    /**
     * 主键ID
     * 自增类型，作为用户唯一标识
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     * 用于登录系统的唯一用户名
     */
    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    /**
     * 密码(加密存储)
     * 用户登录密码，数据库中应加密存储
     */
    @Schema(description = "密码(加密存储)")
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     * 用户的真实姓名信息
     */
    @Schema(description = "真实姓名")
    @TableField("realName")
    private String realName;

    /**
     * 角色
     * 用户在系统中的角色类型，可选值为：
     * - ADMIN: 管理员
     * - LANDLORD: 房东
     * - TENANT: 租户
     */
    @Schema(description = "角色: ADMIN(管理员), LANDLORD(房东), TENANT(租户)")
    @TableField("role")
    private String role;

    /**
     * 联系电话
     * 用户的手机号码，用于联系方式验证
     */
    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    /**
     * 头像URL
     * 用户头像的图片链接地址
     */
    @Schema(description = "头像URL")
    @TableField("avatar")
    private String avatar;

    /**
     * 用户状态
     * 账户的使用状态，可选值为：
     * - normal: 正常
     * - disabled: 禁用
     */
    @Schema(description = "用户状态: normal(正常), disabled(禁用)")
    @TableField("status")
    private String status;

    /**
     * 注册时间
     * 用户账户创建的时间
     */
    @Schema(description = "注册时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 用户信息最后一次更新的时间
     */
    @Schema(description = "更新时间")
    @TableField("updateTime")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     * 用于标记用户是否被删除：
     * - 0: 未删除
     * - 1: 已删除
     * 采用逻辑删除而非物理删除
     */
    @Schema(description = "逻辑删除: 0未删, 1已删")
    @TableLogic
    @TableField("isDeleted")
    private Integer isDeleted;
}
