package com.swapcampus.user.service.impl;

import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserPrincipal;
import com.swapcampus.user.config.UserVerificationProperties;
import com.swapcampus.user.dto.UserProfileUpdateRequest;
import com.swapcampus.user.dto.UserStudentVerifyRequest;
import com.swapcampus.user.entity.CreditRecordEntity;
import com.swapcampus.user.entity.StudentIdentityEntity;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.CreditRecordMapper;
import com.swapcampus.user.mapper.StudentIdentityMapper;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import com.swapcampus.user.vo.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserProfileMapper userProfileMapper;
    @Mock
    private CreditRecordMapper creditRecordMapper;
    @Mock
    private StudentIdentityMapper studentIdentityMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserVerificationProperties verificationProperties;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        verificationProperties = new UserVerificationProperties();
        verificationProperties.setStudentNoPattern("^[0-9]{8}$");
        userService = new UserServiceImpl(
                userMapper,
                userProfileMapper,
                creditRecordMapper,
                studentIdentityMapper,
                auditLogService,
                verificationProperties,
                passwordEncoder
        );
        setCurrentUser(7L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserByIdThrowsWhenUserMissing() {
        when(userMapper.selectById(404L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.getUserById(404L));

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateCurrentUserProfileUpdatesUserProfileAndAuditLog() {
        UserEntity user = activeUser(7L);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(7L);

        when(userMapper.selectById(7L)).thenReturn(user);
        when(userProfileMapper.selectById(7L)).thenReturn(profile);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setPhone(" 13900139000 ");
        request.setEmail(" bob@example.com ");
        request.setBio("  hello campus ");
        request.setAvatarUrl(" https://cdn.example/avatar.png ");

        UserResponse response = userService.updateCurrentUserProfile(request);

        assertEquals("13900139000", user.getPhone());
        assertEquals("bob@example.com", user.getEmail());
        assertEquals("hello campus", profile.getBio());
        assertEquals("https://cdn.example/avatar.png", profile.getAvatarUrl());
        assertEquals(7L, response.getId());

        verify(userMapper).updateById(user);
        verify(userProfileMapper).updateById(profile);
        verify(auditLogService).record(7L, "USER_PROFILE_UPDATE", "USER", 7L, "更新个人资料");
    }

    @Test
    void verifyStudentStoresIdentityFields() {
        UserEntity user = activeUser(7L);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(7L);
        StudentIdentityEntity identity = studentIdentity();

        when(userMapper.selectById(7L)).thenReturn(user);
        when(userProfileMapper.selectById(7L)).thenReturn(profile);
        when(studentIdentityMapper.selectOne(any())).thenReturn(identity);
        when(passwordEncoder.matches("edu-pass", "edu-hash")).thenReturn(true);
        when(userProfileMapper.selectOne(any())).thenReturn(null);

        UserStudentVerifyRequest request = verifyRequest();
        UserResponse response = userService.verifyStudent(request);

        assertEquals("Alice", profile.getRealName());
        assertEquals("20240001", profile.getStudentNo());
        assertEquals("Computer Science", profile.getCollege());
        assertEquals("2024", profile.getGrade());
        assertEquals("202****01", profile.getContactMasked());
        assertNotNull(profile.getVerifiedAt());
        assertEquals("20240001", response.getProfile().getStudentNo());

        verify(userProfileMapper).updateById(profile);
        verify(auditLogService).record(7L, "USER_STUDENT_VERIFY", "USER", 7L, "学号实名验证");
    }

    @Test
    void verifyStudentRejectsWrongEduPassword() {
        UserEntity user = activeUser(7L);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(7L);

        when(userMapper.selectById(7L)).thenReturn(user);
        when(userProfileMapper.selectById(7L)).thenReturn(profile);
        when(studentIdentityMapper.selectOne(any())).thenReturn(studentIdentity());
        when(passwordEncoder.matches("edu-pass", "edu-hash")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.verifyStudent(verifyRequest()));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(userProfileMapper, never()).updateById(any(UserProfileEntity.class));
    }

    @Test
    void verifyStudentRejectsDuplicateStudentNo() {
        UserEntity user = activeUser(7L);
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(7L);
        UserProfileEntity existing = new UserProfileEntity();
        existing.setUserId(8L);
        existing.setStudentNo("20240001");

        when(userMapper.selectById(7L)).thenReturn(user);
        when(userProfileMapper.selectById(7L)).thenReturn(profile);
        when(studentIdentityMapper.selectOne(any())).thenReturn(studentIdentity());
        when(passwordEncoder.matches("edu-pass", "edu-hash")).thenReturn(true);
        when(userProfileMapper.selectOne(any())).thenReturn(existing);

        BusinessException exception = assertThrows(BusinessException.class, () -> userService.verifyStudent(verifyRequest()));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(userProfileMapper, never()).updateById(any(UserProfileEntity.class));
    }

    @Test
    void adjustCreditScoreClampsScoreAndCreatesRecord() {
        UserEntity user = activeUser(7L);
        user.setCreditScore(95);
        when(userMapper.selectById(7L)).thenReturn(user);

        userService.adjustCreditScore(7L, 20, " good trade ", "ORDER", 99L);

        assertEquals(100, user.getCreditScore());
        verify(userMapper).updateById(user);

        ArgumentCaptor<CreditRecordEntity> recordCaptor = ArgumentCaptor.forClass(CreditRecordEntity.class);
        verify(creditRecordMapper).insert(recordCaptor.capture());
        CreditRecordEntity record = recordCaptor.getValue();
        assertEquals(7L, record.getUserId());
        assertEquals(20, record.getDelta());
        assertEquals(100, record.getScoreAfter());
        assertEquals("good trade", record.getReason());
        assertEquals("ORDER", record.getRefType());
        assertEquals(99L, record.getRefId());
        assertNotNull(record.getCreatedAt());
    }

    private void setCurrentUser(Long userId) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal(userId, "alice", Role.USER);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null, "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UserStudentVerifyRequest verifyRequest() {
        UserStudentVerifyRequest request = new UserStudentVerifyRequest();
        request.setRealName("Alice");
        request.setStudentNo("20240001");
        request.setEduPassword("edu-pass");
        return request;
    }

    private StudentIdentityEntity studentIdentity() {
        StudentIdentityEntity identity = new StudentIdentityEntity();
        identity.setId(1L);
        identity.setStudentNo("20240001");
        identity.setRealName("Alice");
        identity.setCollege("Computer Science");
        identity.setGrade("2024");
        identity.setEduPasswordHash("edu-hash");
        identity.setStatus("ACTIVE");
        return identity;
    }

    private UserEntity activeUser(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername("alice");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreditScore(60);
        user.setPointBalance(0);
        return user;
    }
}
