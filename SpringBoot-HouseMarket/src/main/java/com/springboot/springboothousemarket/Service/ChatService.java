package com.springboot.springboothousemarket.Service;

import com.springboot.springboothousemarket.Entity.ChatMessage;
import com.springboot.springboothousemarket.dto.ConversationVO;

import java.util.List;

public interface ChatService {

    /**
     * 发送消息：先落库（发送即持久化），再尝试 WebSocket 实时推送（失败不影响消息留存）。
     */
    ChatMessage send(Long senderId, Long receiverId, Long houseId, String content);

    /**
     * 与某伙伴的历史消息（只允许查自己参与的会话），按时间正序返回。
     */
    List<ChatMessage> history(Long userId, Long partnerId, Long houseId, int page, int pageSize);

    /**
     * 当前用户的会话列表：按伙伴聚合，含最近消息与未读数。
     */
    List<ConversationVO> conversations(Long userId);

    long unreadCount(Long userId);

    /**
     * 将伙伴发给该用户的未读消息标记已读。
     */
    int markRead(Long userId, Long partnerId);

    /**
     * 删除某用户相关的全部聊天消息（账号注销/删除时清理）。
     */
    void deleteByUser(Long userId);

    /**
     * 删除某房源相关的聊天上下文引用（房源删除时调用，消息本身保留）。
     */
    void clearHouseReference(Long houseId);
}
