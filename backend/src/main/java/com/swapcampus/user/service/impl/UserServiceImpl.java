package com.swapcampus.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.audit.service.AuditLogService;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.user.dto.VerifyStudentRequest;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import com.swapcampus.user.service.UserService;
import com.swapcampus.user.vo.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final AuditLogService auditLogService;

    public UserServiceImpl(UserMapper userMapper,
                           UserProfileMapper userProfileMapper,
                           AuditLogService auditLogService) {
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public String moduleName() {
        return "user";
    }

    @Override
    public UserResponse getCurrentUser() {
        return getUserById(CurrentUserContext.requireUserId());
    }

    @Override
    public UserResponse getUserById(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        UserProfileEntity profile = userProfileMapper.selectById(userId);
        return UserResponse.from(user, profile);
    }

    @Override
    @Transactional
    public UserResponse verifyStudent(Long userId, VerifyStudentRequest request) {
        UserProfileEntity profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            profile = new UserProfileEntity();
            profile.setUserId(userId);
        }
        if (profile.getVerifiedAt() != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您已完成学生认证");
        }

        String studentNo = request.getStudentNo().trim();
        Long duplicateCount = userProfileMapper.selectCount(new LambdaQueryWrapper<UserProfileEntity>()
                .eq(UserProfileEntity::getStudentNo, studentNo)
                .ne(UserProfileEntity::getUserId, userId));
        if (duplicateCount != null && duplicateCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该学号已被其他账号绑定");
        }

        profile.setStudentNo(studentNo);
        profile.setRealName(request.getRealName().trim());
        profile.setCollege(request.getCollege().trim());
        profile.setGrade(request.getGrade() == null ? null : request.getGrade().trim());
        profile.setVerifiedAt(LocalDateTime.now());

        if (userProfileMapper.selectById(userId) == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }

        auditLogService.record(userId, "USER_VERIFY_STUDENT", "USER", userId,
                "学号实名验证通过: " + studentNo);
        return getUserById(userId);
    }
}
