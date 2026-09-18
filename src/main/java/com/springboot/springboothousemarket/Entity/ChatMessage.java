package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息（持久化）：发送即落库，WebSocket 仅作实时触达，历史消息以本表为准。
 */
@Data
@Schema(description = "聊天消息")
@TableName("chat_message")
public class ChatMessage {

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "发送者用户ID")
    @TableField("sender_id")
    private Long senderId;

    @Schema(description = "接收者用户ID")
    @TableField("receiver_id")
    private Long receiverId;

    @Schema(description = "关联房源ID")
    @TableField("house_id")
    private Long houseId;

    @Schema(description = "消息内容")
    @TableField("content")
    private String content;

    @Schema(description = "消息类型: TEXT/SYSTEM")
    @TableField("message_type")
    private String messageType;

    @Schema(description = "已读状态: 0未读, 1已读")
    @TableField("read_status")
    private Integer readStatus;

    @Schema(description = "发送时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 关联属性（非数据库字段）
    @Schema(description = "发送者昵称")
    @TableField(exist = false)
    private String senderName;

    @Schema(description = "房源标题")
    @TableField(exist = false)
    private String houseTitle;
}
