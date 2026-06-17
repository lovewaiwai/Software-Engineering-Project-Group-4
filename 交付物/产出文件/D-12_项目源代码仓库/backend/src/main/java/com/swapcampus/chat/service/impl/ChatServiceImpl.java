package com.swapcampus.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapcampus.chat.dto.CreateSessionRequest;
import com.swapcampus.chat.dto.SendMessageRequest;
import com.swapcampus.chat.entity.ChatMessageEntity;
import com.swapcampus.chat.entity.ChatSessionEntity;
import com.swapcampus.chat.mapper.ChatMessageMapper;
import com.swapcampus.chat.mapper.ChatSessionMapper;
import com.swapcampus.chat.service.ChatService;
import com.swapcampus.chat.vo.ChatMessageResponse;
import com.swapcampus.chat.vo.ChatSessionResponse;
import com.swapcampus.chat.websocket.ChatWebSocketSessionRegistry;
import com.swapcampus.common.enums.MessageStatus;
import com.swapcampus.common.enums.MessageType;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.moderation.ContentModerationService;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.entity.ProductImageEntity;
import com.swapcampus.product.mapper.ProductImageMapper;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.service.UserModerationService;
import com.swapcampus.user.service.UserVerificationGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ChatServiceImpl implements ChatService {

    private static final int DEFAULT_MESSAGE_LIMIT = 30;

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final UserModerationService userModerationService;
    private final UserVerificationGuard userVerificationGuard;
    private final ContentModerationService contentModerationService;
    private final ChatWebSocketSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    public ChatServiceImpl(ChatSessionMapper chatSessionMapper,
                           ChatMessageMapper chatMessageMapper,
                           UserMapper userMapper,
                           ProductMapper productMapper,
                           ProductImageMapper productImageMapper,
                           UserModerationService userModerationService,
                           UserVerificationGuard userVerificationGuard,
                           ContentModerationService contentModerationService,
                           ChatWebSocketSessionRegistry sessionRegistry,
                           ObjectMapper objectMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.userModerationService = userModerationService;
        this.userVerificationGuard = userVerificationGuard;
        this.contentModerationService = contentModerationService;
        this.sessionRegistry = sessionRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ChatSessionResponse createOrGetSession(Long userId, CreateSessionRequest request) {
        userVerificationGuard.requireVerifiedStudent(userId);
        Long sellerId = request.getSellerId();
        if (Objects.equals(userId, sellerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能与自己发起聊天");
        }
        userModerationService.ensureCanChat(userId, sellerId);

        ChatSessionEntity existing = findExistingSession(userId, sellerId, request.getProductId());
        if (existing != null) {
            return enrichSession(existing, userId);
        }

        ChatSessionEntity session = new ChatSessionEntity();
        session.setProductId(request.getProductId());
        session.setBuyerId(userId);
        session.setSellerId(sellerId);
        session.setCreatedAt(LocalDateTime.now());
        chatSessionMapper.insert(session);
        return enrichSession(session, userId);
    }

    @Override
    public List<ChatSessionResponse> listSessions(Long userId) {
        List<ChatSessionEntity> sessions = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSessionEntity>()
                .and(wrapper -> wrapper.eq(ChatSessionEntity::getBuyerId, userId)
                        .or()
                        .eq(ChatSessionEntity::getSellerId, userId))
                .orderByDesc(ChatSessionEntity::getLastMessageAt)
                .orderByDesc(ChatSessionEntity::getCreatedAt));
        List<ChatSessionResponse> responses = new ArrayList<>();
        for (ChatSessionEntity session : sessions) {
            responses.add(enrichSession(session, userId));
        }
        return responses;
    }

    @Override
    public List<ChatMessageResponse> listMessages(Long userId, Long sessionId, int limit) {
        ChatSessionEntity session = requireParticipantSession(userId, sessionId);
        int pageSize = limit > 0 ? Math.min(limit, 50) : DEFAULT_MESSAGE_LIMIT;
        List<ChatMessageEntity> messages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, session.getId())
                .orderByDesc(ChatMessageEntity::getSeqNo)
                .last("OFFSET 0 ROWS FETCH NEXT " + pageSize + " ROWS ONLY"));
        List<ChatMessageResponse> responses = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            responses.add(toClientMessage(messages.get(i)));
        }
        return responses;
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long sessionId, SendMessageRequest request) {
        userVerificationGuard.requireVerifiedStudent(userId);
        ChatSessionEntity session = requireParticipantSession(userId, sessionId);
        Long peerId = peerId(session, userId);
        userModerationService.ensureCanChat(userId, peerId);
        validateMessageRequest(request);

        if (request.getMessageType() == MessageType.TEXT) {
            contentModerationService.checkText(request.getContent());
        } else if (request.getMessageType() == MessageType.IMAGE) {
            contentModerationService.checkImageUrl(request.getImageUrl());
        }

        ChatMessageEntity message = persistMessage(userId, session, request);
        ChatMessageResponse response = toClientMessage(message);
        pushMessage(peerId, response);
        pushAck(userId, response);
        return response;
    }

    @Override
    @Transactional
    public List<ChatMessageResponse> markSessionRead(Long userId, Long sessionId) {
        ChatSessionEntity session = requireParticipantSession(userId, sessionId);
        LocalDateTime now = LocalDateTime.now();

        List<ChatMessageEntity> unread = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .ne(ChatMessageEntity::getSenderId, userId)
                .isNull(ChatMessageEntity::getReadAt)
                .orderByAsc(ChatMessageEntity::getSeqNo));

        if (unread.isEmpty()) {
            return List.of();
        }

        chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, sessionId)
                .ne(ChatMessageEntity::getSenderId, userId)
                .isNull(ChatMessageEntity::getReadAt)
                .set(ChatMessageEntity::getReadAt, now)
                .set(ChatMessageEntity::getStatus, MessageStatus.READ));

        List<ChatMessageResponse> readMessages = unread.stream().map(entity -> {
            entity.setReadAt(now);
            entity.setStatus(MessageStatus.READ);
            return ChatMessageResponse.from(entity);
        }).toList();

        Long senderId = unread.get(0).getSenderId();
        pushReadReceipt(senderId, sessionId, readMessages);
        return readMessages;
    }

    @Override
    @Transactional
    public ChatMessageResponse recallMessage(Long userId, Long messageId) {
        ChatMessageEntity message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "消息不存在");
        }
        if (!Objects.equals(message.getSenderId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能撤回自己的消息");
        }
        if (message.getStatus() == MessageStatus.RECALLED) {
            return toClientMessage(message);
        }
        message.setStatus(MessageStatus.RECALLED);
        message.setContent("[已撤回]");
        chatMessageMapper.updateById(message);
        ChatMessageResponse response = toClientMessage(message);
        ChatSessionEntity session = chatSessionMapper.selectById(message.getSessionId());
        if (session != null) {
            Long peerId = peerId(session, userId);
            pushRecall(peerId, response);
        }
        return response;
    }

    public ChatMessageResponse sendMessageFromWebSocket(Long userId, Long sessionId, SendMessageRequest request) {
        return sendMessage(userId, sessionId, request);
    }

    public List<ChatMessageResponse> markSessionReadFromWebSocket(Long userId, Long sessionId) {
        return markSessionRead(userId, sessionId);
    }

    private ChatSessionEntity findExistingSession(Long userId, Long sellerId, Long productId) {
        LambdaQueryWrapper<ChatSessionEntity> wrapper = new LambdaQueryWrapper<ChatSessionEntity>()
                .and(w -> w.and(x -> x.eq(ChatSessionEntity::getBuyerId, userId).eq(ChatSessionEntity::getSellerId, sellerId))
                        .or(x -> x.eq(ChatSessionEntity::getBuyerId, sellerId).eq(ChatSessionEntity::getSellerId, userId)));
        if (productId != null) {
            wrapper.eq(ChatSessionEntity::getProductId, productId);
        }
        wrapper.orderByDesc(ChatSessionEntity::getCreatedAt).last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY");
        return chatSessionMapper.selectOne(wrapper);
    }

    private ChatSessionEntity requireParticipantSession(Long userId, Long sessionId) {
        ChatSessionEntity session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        if (!Objects.equals(session.getBuyerId(), userId) && !Objects.equals(session.getSellerId(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该会话");
        }
        return session;
    }

    private ChatSessionResponse enrichSession(ChatSessionEntity session, Long userId) {
        ChatSessionResponse response = ChatSessionResponse.from(session, userId);
        UserEntity peer = userMapper.selectById(response.getPeerId());
        if (peer != null) {
            response.setPeerUsername(peer.getUsername());
        }
        if (session.getProductId() != null) {
            ProductEntity product = productMapper.selectById(session.getProductId());
            if (product != null) {
                response.setProductTitle(product.getTitle());
                response.setProductPrice(product.getPrice());
                response.setProductStatus(product.getStatus());
            }
            ProductImageEntity cover = productImageMapper.selectOne(new LambdaQueryWrapper<ProductImageEntity>()
                    .eq(ProductImageEntity::getProductId, session.getProductId())
                    .orderByAsc(ProductImageEntity::getSortOrder)
                    .orderByAsc(ProductImageEntity::getId)
                    .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
            if (cover != null) {
                response.setProductImageUrl(cover.getUrl());
            }
        }
        ChatMessageEntity lastMessage = chatMessageMapper.selectOne(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, session.getId())
                .orderByDesc(ChatMessageEntity::getSeqNo)
                .last("OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY"));
        if (lastMessage != null) {
            response.setLastPreview(preview(lastMessage));
        }
        Long unreadCount = chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessageEntity>()
                .eq(ChatMessageEntity::getSessionId, session.getId())
                .ne(ChatMessageEntity::getSenderId, userId)
                .isNull(ChatMessageEntity::getReadAt));
        response.setUnreadCount(unreadCount == null ? 0 : unreadCount.intValue());
        return response;
    }

    private ChatMessageEntity persistMessage(Long userId, ChatSessionEntity session, SendMessageRequest request) {
        Long seqNo = chatMessageMapper.selectMaxSeqNo(session.getId()) + 1;
        ChatMessageEntity message = new ChatMessageEntity();
        message.setSessionId(session.getId());
        message.setSenderId(userId);
        message.setMessageType(request.getMessageType());
        message.setContent(resolveContent(request));
        message.setImageUrl(request.getImageUrl());
        message.setSeqNo(seqNo);
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(message);

        session.setLastMessageAt(message.getCreatedAt());
        chatSessionMapper.updateById(session);
        return message;
    }

    private void validateMessageRequest(SendMessageRequest request) {
        if (request.getMessageType() == MessageType.TEXT && !request.hasContent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文本消息不能为空");
        }
        if (request.getMessageType() == MessageType.IMAGE
                && (request.getImageUrl() == null || request.getImageUrl().isBlank())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片消息缺少图片地址");
        }
        if (request.getMessageType() == MessageType.EMOJI && !request.hasContent()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "表情消息不能为空");
        }
    }

    private String resolveContent(SendMessageRequest request) {
        if (request.getMessageType() == MessageType.IMAGE) {
            return request.getContent() == null ? "[图片]" : request.getContent();
        }
        if (request.getMessageType() == MessageType.EMOJI) {
            return request.getContent() == null ? "[表情]" : request.getContent();
        }
        return request.getContent();
    }

    private ChatMessageResponse toClientMessage(ChatMessageEntity entity) {
        ChatMessageResponse response = ChatMessageResponse.from(entity);
        if (entity.getStatus() == MessageStatus.RECALLED) {
            response.setContent("[已撤回]");
        }
        return response;
    }

    private String preview(ChatMessageEntity message) {
        if (message.getStatus() == MessageStatus.RECALLED) {
            return "[已撤回]";
        }
        if (message.getMessageType() == MessageType.IMAGE) {
            return "[图片]";
        }
        if (message.getMessageType() == MessageType.EMOJI) {
            return "[表情]";
        }
        String content = message.getContent();
        return content == null ? "" : (content.length() > 30 ? content.substring(0, 30) + "..." : content);
    }

    private Long peerId(ChatSessionEntity session, Long userId) {
        return Objects.equals(session.getBuyerId(), userId) ? session.getSellerId() : session.getBuyerId();
    }

    private void pushMessage(Long peerId, ChatMessageResponse message) {
        sessionRegistry.sendToUser(peerId, json(Map.of(
                "type", "CHAT_MESSAGE",
                "message", message
        )));
    }

    private void pushAck(Long senderId, ChatMessageResponse message) {
        sessionRegistry.sendToUser(senderId, json(Map.of(
                "type", "MESSAGE_ACK",
                "message", message
        )));
    }

    private void pushReadReceipt(Long senderId, Long sessionId, List<ChatMessageResponse> messages) {
        sessionRegistry.sendToUser(senderId, json(Map.of(
                "type", "READ_RECEIPT",
                "sessionId", sessionId,
                "messages", messages
        )));
    }

    private void pushRecall(Long peerId, ChatMessageResponse message) {
        sessionRegistry.sendToUser(peerId, json(Map.of(
                "type", "MESSAGE_RECALLED",
                "message", message
        )));
    }

    private String json(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "消息序列化失败");
        }
    }
}
