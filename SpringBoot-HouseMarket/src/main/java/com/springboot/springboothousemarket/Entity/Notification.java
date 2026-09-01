package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户通知：通知中心的持久化数据源。
 * 用户可见状态（未读/已读）以本表为准，WebSocket 实时推送只是加速触达的手段。
 */
@Data
@Schema(description = "用户通知")
@TableName("notification")
public class Notification {

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "接收用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "通知类型")
    @TableField("type")
    private String type;

    @Schema(description = "通知标题")
    @TableField("title")
    private String title;

    @Schema(description = "通知内容")
    @TableField("content")
    private String content;

    @Schema(description = "关联业务类型: APPOINTMENT/LANDLORD_APPLICATION/IDENTITY_VERIFICATION/HOUSE")
    @TableField("related_type")
    private String relatedType;

    @Schema(description = "关联业务ID")
    @TableField("related_id")
    private Long relatedId;

    @Schema(description = "已读状态: 0未读, 1已读")
    @TableField("read_status")
    private Integer readStatus;

    @Schema(description = "WebSocket实时推送时间，为空表示用户当时离线")
    @TableField("sent_time")
    private LocalDateTime sentTime;

    @Schema(description = "推送重试次数")
    @TableField("retry_count")
    private Integer retryCount;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
