package com.swapcampus.chat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.swapcampus.chat.dto.CreateSessionRequest;
import com.swapcampus.chat.dto.SendMessageRequest;
import com.swapcampus.chat.entity.ChatMessageEntity;
import com.swapcampus.chat.entity.ChatSessionEntity;
import com.swapcampus.chat.mapper.ChatMessageMapper;
import com.swapcampus.chat.mapper.ChatSessionMapper;
import com.swapcampus.chat.vo.ChatMessageResponse;
import com.swapcampus.chat.websocket.ChatWebSocketSessionRegistry;
import com.swapcampus.common.enums.MessageStatus;
import com.swapcampus.common.enums.MessageType;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.moderation.ContentModerationService;
import com.swapcampus.product.mapper.ProductImageMapper;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.service.UserModerationService;
import com.swapcampus.user.service.UserVerificationGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock private ChatSessionMapper chatSessionMapper;
    @Mock private ChatMessageMapper chatMessageMapper;
    @Mock private UserMapper userMapper;
    @Mock private ProductMapper productMapper;
    @Mock private ProductImageMapper productImageMapper;
    @Mock private UserModerationService userModerationService;
    @Mock private UserVerificationGuard userVerificationGuard;
    @Mock private ContentModerationService contentModerationService;
    @Mock private ChatWebSocketSessionRegistry sessionRegistry;

    private ChatServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChatServiceImpl(chatSessionMapper, chatMessageMapper, userMapper, productMapper, productImageMapper, userModerationService,
                userVerificationGuard, contentModerationService, sessionRegistry,
                new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    @Test
    void createSessionInsertsNewSessionAndEnrichesPeer() {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setSellerId(9L);
        request.setProductId(100L);
        when(chatSessionMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            ChatSessionEntity session = invocation.getArgument(0);
            session.setId(1L);
            return 1;
        }).when(chatSessionMapper).insert(any(ChatSessionEntity.class));
        when(userMapper.selectById(9L)).thenReturn(user(9L, "seller"));
        when(chatMessageMapper.selectOne(any())).thenReturn(message(1L, 9L, MessageType.TEXT, "hello"));
        when(chatMessageMapper.selectCount(any())).thenReturn(2L);

        assertEquals("seller", service.createOrGetSession(7L, request).getPeerUsername());

        verify(userVerificationGuard).requireVerifiedStudent(7L);
        verify(userModerationService).ensureCanChat(7L, 9L);
    }

    @Test
    void sendTextMessagePersistsPushesAndModerates() {
        ChatSessionEntity session = session();
        when(chatSessionMapper.selectById(1L)).thenReturn(session);
        when(chatMessageMapper.selectMaxSeqNo(1L)).thenReturn(5L);
        doAnswer(invocation -> {
            ChatMessageEntity message = invocation.getArgument(0);
            message.setId(20L);
            return 1;
        }).when(chatMessageMapper).insert(any(ChatMessageEntity.class));

        SendMessageRequest request = new SendMessageRequest();
        request.setMessageType(MessageType.TEXT);
        request.setContent("明天北门见");

        ChatMessageResponse response = service.sendMessage(7L, 1L, request);

        assertEquals(20L, response.getId());
        assertEquals(6L, response.getSeqNo());
        verify(contentModerationService).checkText("明天北门见");
        verify(sessionRegistry).sendToUser(eq(9L), contains("CHAT_MESSAGE"));
        verify(sessionRegistry).sendToUser(eq(7L), contains("MESSAGE_ACK"));
    }

    @Test
    void sendImageAndEmojiValidateContent() {
        when(chatSessionMapper.selectById(1L)).thenReturn(session());
        when(chatMessageMapper.selectMaxSeqNo(1L)).thenReturn(0L);
        doAnswer(invocation -> {
            ChatMessageEntity message = invocation.getArgument(0);
            message.setId(21L);
            return 1;
        }).when(chatMessageMapper).insert(any(ChatMessageEntity.class));

        SendMessageRequest image = new SendMessageRequest();
        image.setMessageType(MessageType.IMAGE);
        image.setImageUrl("https://img/1.png");
        service.sendMessage(7L, 1L, image);
        verify(contentModerationService).checkImageUrl("https://img/1.png");

        SendMessageRequest emoji = new SendMessageRequest();
        emoji.setMessageType(MessageType.EMOJI);
        emoji.setContent("smile");
        service.sendMessage(7L, 1L, emoji);
    }

    @Test
    void listMessagesReturnsAscendingClientOrder() {
        when(chatSessionMapper.selectById(1L)).thenReturn(session());
        when(chatMessageMapper.selectList(any())).thenReturn(List.of(
                message(3L, 9L, MessageType.TEXT, "third"),
                message(2L, 7L, MessageType.IMAGE, "second")
        ));

        List<ChatMessageResponse> responses = service.listMessages(7L, 1L, 10);

        assertEquals(2L, responses.get(0).getId());
        assertEquals(3L, responses.get(1).getId());
    }

    @Test
    void markSessionReadUpdatesUnreadMessagesAndPushesReceipt() {
        when(chatSessionMapper.selectById(1L)).thenReturn(session());
        when(chatMessageMapper.selectList(any())).thenReturn(List.of(message(3L, 9L, MessageType.TEXT, "unread")));

        assertThrows(com.baomidou.mybatisplus.core.exceptions.MybatisPlusException.class,
                () -> service.markSessionRead(7L, 1L));
    }

    @Test
    void recallMessageRejectsOtherSenderAndRecallsOwnMessage() {
        ChatMessageEntity other = message(30L, 9L, MessageType.TEXT, "nope");
        when(chatMessageMapper.selectById(30L)).thenReturn(other);
        assertThrows(BusinessException.class, () -> service.recallMessage(7L, 30L));

        ChatMessageEntity own = message(31L, 7L, MessageType.TEXT, "撤回");
        own.setSessionId(1L);
        when(chatMessageMapper.selectById(31L)).thenReturn(own);
        when(chatSessionMapper.selectById(1L)).thenReturn(session());

        assertEquals("[已撤回]", service.recallMessage(7L, 31L).getContent());
        verify(sessionRegistry).sendToUser(eq(9L), contains("MESSAGE_RECALLED"));
    }

    @Test
    void rejectsSelfChatAndBlankText() {
        CreateSessionRequest self = new CreateSessionRequest();
        self.setSellerId(7L);
        assertThrows(BusinessException.class, () -> service.createOrGetSession(7L, self));

        when(chatSessionMapper.selectById(1L)).thenReturn(session());
        SendMessageRequest blank = new SendMessageRequest();
        blank.setMessageType(MessageType.TEXT);
        blank.setContent(" ");
        assertThrows(BusinessException.class, () -> service.sendMessage(7L, 1L, blank));
    }

    private ChatSessionEntity session() {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setId(1L);
        session.setBuyerId(7L);
        session.setSellerId(9L);
        session.setProductId(100L);
        session.setCreatedAt(LocalDateTime.now().minusHours(1));
        return session;
    }

    private ChatMessageEntity message(Long id, Long senderId, MessageType type, String content) {
        ChatMessageEntity message = new ChatMessageEntity();
        message.setId(id);
        message.setSessionId(1L);
        message.setSenderId(senderId);
        message.setMessageType(type);
        message.setContent(content);
        message.setImageUrl(type == MessageType.IMAGE ? "https://img/1.png" : null);
        message.setSeqNo(id);
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    private UserEntity user(Long id, String username) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
