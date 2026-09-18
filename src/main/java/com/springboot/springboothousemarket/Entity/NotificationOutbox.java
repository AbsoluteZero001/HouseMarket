package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "通知事务 Outbox")
@TableName("notification_outbox")
public class NotificationOutbox {

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "业务幂等键")
    @TableField("business_key")
    private String businessKey;

    @Schema(description = "业务类型: APPOINTMENT/LANDLORD")
    @TableField("business_type")
    private String businessType;

    @Schema(description = "预约ID")
    @TableField("appointment_id")
    private Long appointmentId;

    @Schema(description = "关联的用户通知ID")
    @TableField("notification_id")
    private Long notificationId;

    @Schema(description = "事件类型")
    @TableField("event_type")
    private String eventType;

    @Schema(description = "通知负载 JSON")
    @TableField("payload")
    private String payload;

    @Schema(description = "通知目标用户ID")
    @TableField("target_user_id")
    private Long targetUserId;

    @Schema(description = "状态: pending/processing/sent/failed")
    @TableField("status")
    private String status;

    @Schema(description = "重试次数")
    @TableField("retry_count")
    private Integer retryCount;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "发送时间")
    @TableField("send_time")
    private LocalDateTime sendTime;
}
