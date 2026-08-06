package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "房东入驻申请")
@TableName("landlord_application")
public class LandlordApplication {

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "申请人用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "申请人用户名")
    @TableField("username")
    private String username;

    @Schema(description = "实名信息")
    @TableField("real_name")
    private String realName;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "状态: pending/approved/rejected")
    @TableField("status")
    private String status;

    @Schema(description = "审核意见")
    @TableField("review_note")
    private String reviewNote;

    @Schema(description = "审核人ID")
    @TableField("reviewer_id")
    private Long reviewerId;

    @Schema(description = "审核时间")
    @TableField("review_time")
    private LocalDateTime reviewTime;

    @Schema(description = "申请时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
