package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.ChatMessage;
import com.springboot.springboothousemarket.Entity.Houses;
import com.springboot.springboothousemarket.Entity.Users;
import com.springboot.springboothousemarket.Mapper.ChatMessageMapper;
import com.springboot.springboothousemarket.dto.BusinessException;
import com.springboot.springboothousemarket.dto.ConversationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private static final int MAX_CONTENT_LENGTH = 1000;

    private final UsersService usersService;
    private final HousesService housesService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(UsersService usersService, HousesService housesService,
                           SimpMessagingTemplate messagingTemplate) {
        this.usersService = usersService;
        this.housesService = housesService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public ChatMessage send(Long senderId, Long receiverId, Long houseId, String content) {
        if (senderId == null || receiverId == null) {
            throw new BusinessException("发送双方不能为空");
        }
        if (senderId.equals(receiverId)) {
            throw new BusinessException("不能给自己发消息");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException("消息内容不能为空");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("消息内容不能超过" + MAX_CONTENT_LENGTH + "个字符");
        }
        Users receiver = usersService.getUserById(receiverId);
        if (receiver == null) {
            throw new BusinessException("接收用户不存在");
        }
        if (houseId != null) {
            Houses house = housesService.getHouseById(houseId);
            if (house == null) {
                throw new BusinessException("会话关联的房源不存在");
            }
        }

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setHouseId(houseId);
        message.setContent(trimmed);
        message.setMessageType("TEXT");
        message.setReadStatus(0);
        message.setCreateTime(java.time.LocalDateTime.now());
        save(message);

        // 先落库成功，再做实时推送；推送失败不影响消息留存（对方打开会话可从历史消息读取）
        try {
            messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/chat", decorate(message));
            messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/chat", decorate(message));
        } catch (Exception e) {
            log.warn("聊天消息实时推送失败(消息已落库), id={}, reason={}", message.getId(), e.getMessage());
        }
        return decorate(message);
    }

    @Override
    public List<ChatMessage> history(Long userId, Long partnerId, Long houseId, int page, int pageSize) {
        if (partnerId == null) {
            throw new BusinessException("会话伙伴不能为空");
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        List<ChatMessage> messages = baseMapper.selectHistory(userId, partnerId, houseId,
                (safePage - 1) * safeSize, safeSize);
        // SQL 为倒序取最新一页，此处翻转为时间正序返回
        java.util.Collections.reverse(messages);
        return messages.stream().map(this::decorate).toList();
    }

    @Override
    public List<ConversationVO> conversations(Long userId) {
        List<ChatMessage> lastMessages = baseMapper.selectLastMessagePerPartner(userId);
        if (lastMessages.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> unreadBySender = new HashMap<>();
        for (Map<String, Object> row : baseMapper.countUnreadBySender(userId)) {
            Object senderId = row.get("senderId");
            Object unread = row.get("unread");
            if (senderId != null && unread != null) {
                unreadBySender.put(Long.valueOf(senderId.toString()), Long.valueOf(unread.toString()));
            }
        }

        List<ConversationVO> result = new ArrayList<>();
        for (ChatMessage last : lastMessages) {
            Long partnerId = last.getSenderId().equals(userId) ? last.getReceiverId() : last.getSenderId();
            Users partner = usersService.getUserById(partnerId);
            if (partner == null) {
                continue;
            }
            ConversationVO vo = new ConversationVO();
            vo.setPartnerId(partnerId);
            vo.setPartnerName(displayName(partner));
            vo.setLastMessage(last.getContent());
            vo.setLastTime(last.getCreateTime());
            vo.setLastFromMe(last.getSenderId().equals(userId));
            vo.setUnreadCount(unreadBySender.getOrDefault(partnerId, 0L));
            vo.setHouseId(last.getHouseId());
            if (last.getHouseId() != null) {
                Houses house = housesService.getHouseById(last.getHouseId());
                vo.setHouseTitle(house != null ? house.getTitle() : null);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public long unreadCount(Long userId) {
        return lambdaQuery()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getReadStatus, 0)
                .count();
    }

    @Override
    public int markRead(Long userId, Long partnerId) {
        if (partnerId == null) {
            throw new BusinessException("会话伙伴不能为空");
        }
        return baseMapper.markPartnerMessagesRead(userId, partnerId);
    }

    @Override
    @Transactional
    public void deleteByUser(Long userId) {
        remove(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSenderId, userId)
                .or()
                .eq(ChatMessage::getReceiverId, userId));
    }

    @Override
    public void clearHouseReference(Long houseId) {
        lambdaUpdate()
                .eq(ChatMessage::getHouseId, houseId)
                .set(ChatMessage::getHouseId, null)
                .update();
    }

    private String displayName(Users user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }

    private ChatMessage decorate(ChatMessage message) {
        Users sender = usersService.getUserById(message.getSenderId());
        message.setSenderName(sender != null ? displayName(sender) : "用户");
        if (message.getHouseId() != null) {
            Houses house = housesService.getHouseById(message.getHouseId());
            message.setHouseTitle(house != null ? house.getTitle() : null);
        }
        return message;
    }
}
