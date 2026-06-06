package com.swapcampus.auth.service.impl;

import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.auth.dto.LoginRequest;
import com.swapcampus.auth.dto.RegisterRequest;
import com.swapcampus.auth.vo.AuthResponse;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.JwtTokenProvider;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserProfileMapper userProfileMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuditLogService auditLogService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userMapper,
                userProfileMapper,
                passwordEncoder,
                jwtTokenProvider,
                auditLogService
        );
    }

    @Test
    void registerCreatesUserProfileAuditLogAndToken() {
        RegisterRequest request = registerRequest();
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        doAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(100L);
            return 1;
        }).when(userMapper).insert(any(UserEntity.class));
        when(jwtTokenProvider.createToken(eq("100"), any(Map.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(100L, response.getUser().getId());
        assertEquals("alice", response.getUser().getUsername());
        assertEquals(Role.USER, response.getUser().getRole());
        assertEquals(UserStatus.ACTIVE, response.getUser().getStatus());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userMapper).insert(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertEquals("alice", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPasswordHash());
        assertEquals("13800138000", savedUser.getPhone());
        assertEquals("alice@example.com", savedUser.getEmail());
        assertEquals(60, savedUser.getCreditScore());
        assertEquals(0, savedUser.getPointBalance());

        ArgumentCaptor<UserProfileEntity> profileCaptor = ArgumentCaptor.forClass(UserProfileEntity.class);
        verify(userProfileMapper).insert(profileCaptor.capture());
        assertEquals(100L, profileCaptor.getValue().getUserId());
        verify(auditLogService).record(100L, "AUTH_REGISTER", "USER", 100L, "用户注册");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterRequest request = registerRequest();
        when(userMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(userMapper, never()).insert(any(UserEntity.class));
        verify(userProfileMapper, never()).insert(any(UserProfileEntity.class));
    }

    @Test
    void registerRejectsAdminSelfRegistration() {
        RegisterRequest request = registerRequest();
        request.setRole(Role.ADMIN);
        when(userMapper.selectCount(any())).thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(userMapper, never()).insert(any(UserEntity.class));
    }

    @Test
    void loginReturnsTokenForActiveUser() {
        LoginRequest request = loginRequest();
        UserEntity user = activeUser(7L);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(7L);
        profile.setRealName("Alice");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("secret123", "encoded-password")).thenReturn(true);
        when(userProfileMapper.selectById(7L)).thenReturn(profile);
        when(jwtTokenProvider.createToken(eq("7"), any(Map.class))).thenReturn("login-token");

        AuthResponse response = authService.login(request);

        assertEquals("login-token", response.getToken());
        assertEquals(7L, response.getUser().getId());
        assertNotNull(response.getUser().getProfile());
        assertEquals("Alice", response.getUser().getProfile().getRealName());
        verify(auditLogService).record(7L, "AUTH_LOGIN", "USER", 7L, "用户登录");
    }

    @Test
    void loginRejectsWrongPassword() {
        LoginRequest request = loginRequest();
        UserEntity user = activeUser(7L);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("secret123", "encoded-password")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(jwtTokenProvider, never()).createToken(any(), any());
    }

    @Test
    void loginRejectsBannedUser() {
        LoginRequest request = loginRequest();
        UserEntity user = activeUser(7L);
        user.setStatus(UserStatus.BANNED);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("secret123", "encoded-password")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(jwtTokenProvider, never()).createToken(any(), any());
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(" alice ");
        request.setPassword("secret123");
        request.setPhone(" 13800138000 ");
        request.setEmail(" alice@example.com ");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername(" alice ");
        request.setPassword("secret123");
        return request;
    }

    private UserEntity activeUser(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("alice");
        user.setPasswordHash("encoded-password");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreditScore(60);
        user.setPointBalance(0);
        return user;
    }
}
