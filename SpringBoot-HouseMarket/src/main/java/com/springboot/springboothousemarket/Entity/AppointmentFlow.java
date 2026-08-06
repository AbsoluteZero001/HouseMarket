package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "预约流程轨迹")
@TableName("appointment_flow")
public class AppointmentFlow {

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "预约ID")
    @TableField("appointment_id")
    private Long appointmentId;

    @Schema(description = "原状态")
    @TableField("from_status")
    private String fromStatus;

    @Schema(description = "目标状态")
    @TableField("to_status")
    private String toStatus;

    @Schema(description = "动作: PUBLISH/BOOK/APPROVE/REJECT/CANCEL/COMPLETE/NOTIFY")
    @TableField("action")
    private String action;

    @Schema(description = "操作人ID")
    @TableField("operator_id")
    private Long operatorId;

    @Schema(description = "操作人角色")
    @TableField("operator_role")
    private String operatorRole;

    @Schema(description = "审批意见")
    @TableField("remark")
    private String remark;

    @Schema(description = "发生时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
