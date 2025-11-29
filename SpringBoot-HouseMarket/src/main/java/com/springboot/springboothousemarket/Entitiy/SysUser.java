package com.springboot.springboothousemarket.Entitiy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息")
@Entity
@Table(name = "sys_user")
public class SysUser {
    @Schema(description = "主键ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "用户名")
    @Column(name = "username")
    private String username;

    @Schema(description = "密码(加密存储)")
    @Column(name = "password")
    private String password;

    @Schema(description = "真实姓名")
    @Column(name = "real_name")
    private String realName;

    @Schema(description = "角色: ADMIN(管理员), LANDLORD(房东), TENANT(租户)")
    @Column(name = "role")
    private String role;

    @Schema(description = "联系电话")
    @Column(name = "phone")
    private String phone;

    @Schema(description = "头像URL")
    @Column(name = "avatar")
    private String avatar;

    @Schema(description = "注册时间")
    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除: 0未删, 1已删")
    @Column(name = "is_deleted")
    private Integer isDeleted;
}
