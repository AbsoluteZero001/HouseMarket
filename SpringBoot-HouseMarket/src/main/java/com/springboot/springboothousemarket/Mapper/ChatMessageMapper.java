package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 查询与指定用户存在会话的每个伙伴的最新一条消息（会话列表数据源）。
     */
    List<ChatMessage> selectLastMessagePerPartner(@Param("userId") Long userId);

    /**
     * 统计每个发送方发给该用户的未读消息数。
     *
     * @return senderId -> unreadCount
     */
    List<Map<String, Object>> countUnreadBySender(@Param("userId") Long userId);

    /**
     * 查询两人之间的历史消息（可选按房源过滤），按时间倒序取最新一页。
     */
    List<ChatMessage> selectHistory(@Param("userId") Long userId,
                                    @Param("partnerId") Long partnerId,
                                    @Param("houseId") Long houseId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    /**
     * 将伙伴发给该用户的未读消息全部标记已读。
     */
    int markPartnerMessagesRead(@Param("userId") Long userId, @Param("partnerId") Long partnerId);
}
