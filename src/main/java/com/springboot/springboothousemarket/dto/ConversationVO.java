package com.springboot.springboothousemarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天会话列表项。
 */
@Data
@Schema(description = "聊天会话")
public class ConversationVO {

    @Schema(description = "对方用户ID")
    private Long partnerId;

    @Schema(description = "对方显示名")
    private String partnerName;

    @Schema(description = "最近一条消息内容")
    private String lastMessage;

    @Schema(description = "最近一条消息时间")
    private LocalDateTime lastTime;

    @Schema(description = "最近一条消息是否由我发出")
    private Boolean lastFromMe;

    @Schema(description = "未读消息数")
    private Long unreadCount;

    @Schema(description = "会话关联房源ID（最近消息上下文）")
    private Long houseId;

    @Schema(description = "会话关联房源标题")
    private String houseTitle;
}
