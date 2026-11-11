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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

        // 装饰一次即可复用：旧实现对同一条消息调用 decorate 三次（发送两次 + 返回一次），
        // 每次都要查一遍发送者与房源，等于把同一条消息查了六遍。
        ChatMessage decorated = decorateAll(List.of(message)).get(0);
        // 先落库成功，再做实时推送；推送失败不影响消息留存（对方打开会话可从历史消息读取）
        try {
            messagingTemplate.convertAndSendToUser(receiverId.toString(), "/queue/chat", decorated);
            messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/chat", decorated);
        } catch (Exception e) {
            log.warn("聊天消息实时推送失败(消息已落库), id={}, reason={}", message.getId(), e.getMessage());
        }
        return decorated;
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
        return decorateAll(messages);
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

        // 批量加载会话对手与关联房源：旧实现每个会话各查一次用户 + 一次房源，
        // 20 个会话就是 40 次查询，这里压到固定的 2 次。
        Set<Long> partnerIds = lastMessages.stream()
                .map(m -> m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> houseIds = lastMessages.stream()
                .map(ChatMessage::getHouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Users> partners = loadUsers(partnerIds);
        Map<Long, Houses> houses = loadHouses(houseIds);

        List<ConversationVO> result = new ArrayList<>();
        for (ChatMessage last : lastMessages) {
            Long partnerId = last.getSenderId().equals(userId) ? last.getReceiverId() : last.getSenderId();
            Users partner = partners.get(partnerId);
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
                Houses house = houses.get(last.getHouseId());
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

    /**
     * 批量补齐消息的「发送者昵称」与「关联房源标题」。
     *
     * <p>旧实现逐条消息各查一次发送者 + 一次房源，是典型的 N+1：
     * history 一页 50 条消息 = 100 次数据库往返；会话列表同样逐条查询。
     * 这里改为「两次批量查询 + 内存回填」，查询次数从 2N 降为固定 2 次。
     *
     * @param messages 待装饰的消息列表（会被就地补齐字段）
     * @return 同一个列表
     */
    private List<ChatMessage> decorateAll(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return messages == null ? List.of() : messages;
        }
        Set<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> houseIds = messages.stream()
                .map(ChatMessage::getHouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Users> senders = loadUsers(senderIds);
        Map<Long, Houses> houses = loadHouses(houseIds);

        for (ChatMessage message : messages) {
            Users sender = senders.get(message.getSenderId());
            message.setSenderName(sender != null ? displayName(sender) : "用户");
            if (message.getHouseId() != null) {
                Houses house = houses.get(message.getHouseId());
                message.setHouseTitle(house != null ? house.getTitle() : null);
            }
        }
        return messages;
    }

    /**
     * 批量加载用户（已逻辑删除的账号与 {@code getUserById} 保持一致，不会被加载）。
     */
    private Map<Long, Users> loadUsers(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return usersService.listByIds(ids).stream()
                .collect(Collectors.toMap(Users::getId, user -> user));
    }

    /**
     * 批量加载房源。
     *
     * <p>{@code Houses} 没有 {@code @TableLogic}，批量查询不会自动过滤已删除房源，
     * 因此这里显式过滤，保持与 {@code getHouseById}（已删除返回 null）一致的语义。
     */
    private Map<Long, Houses> loadHouses(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return housesService.listByIds(ids).stream()
                .filter(house -> !Integer.valueOf(1).equals(house.getIsDeleted()))
                .collect(Collectors.toMap(Houses::getId, house -> house));
    }
}
