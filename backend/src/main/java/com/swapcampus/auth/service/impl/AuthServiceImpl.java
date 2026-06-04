package com.swapcampus.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.auth.dto.LoginRequest;
import com.swapcampus.auth.dto.RegisterRequest;
import com.swapcampus.auth.service.AuthService;
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
import com.swapcampus.user.vo.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(UserMapper userMapper,
                           UserProfileMapper userProfileMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuditLogService auditLogService) {
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditLogService = auditLogService;
    }

    @Override
    public String moduleName() {
        return "auth";
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        if (existsByUsername(username)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }

        Role role = request.getRole() == null ? Role.USER : request.getRole();
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(normalizeToNull(request.getPhone()));
        user.setEmail(normalizeToNull(request.getEmail()));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreditScore(60);
        user.setPointBalance(0);
        user.setIsDeleted(false);
        userMapper.insert(user);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(user.getId());
        userProfileMapper.insert(profile);

        auditLogService.record(user.getId(), "AUTH_REGISTER", "USER", user.getId(), "用户注册");
        return buildAuthResponse(user, profile);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = normalize(request.getUsername());
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被封禁");
        }

        UserProfileEntity profile = userProfileMapper.selectById(user.getId());
        auditLogService.record(user.getId(), "AUTH_LOGIN", "USER", user.getId(), "用户登录");
        return buildAuthResponse(user, profile);
    }

    @Override
    public void logout() {
        // JWT 无状态登出先由前端删除 token，后续可接入 token blocklist。
    }

    private AuthResponse buildAuthResponse(UserEntity user, UserProfileEntity profile) {
        String token = jwtTokenProvider.createToken(String.valueOf(user.getId()), Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
        return AuthResponse.login(token, UserResponse.from(user, profile));
    }

    private boolean existsByUsername(String username) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, username));
        return count != null && count > 0;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeToNull(String value) {
        String normalized = normalize(value);
        return normalized == null || normalized.isEmpty() ? null : normalized;
    }
}
