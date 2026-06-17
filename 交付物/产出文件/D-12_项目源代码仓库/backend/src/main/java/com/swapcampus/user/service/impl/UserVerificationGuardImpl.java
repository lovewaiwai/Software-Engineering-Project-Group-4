package com.swapcampus.user.service.impl;

import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import com.swapcampus.user.service.UserVerificationGuard;
import org.springframework.stereotype.Service;

@Service
public class UserVerificationGuardImpl implements UserVerificationGuard {

    private final UserProfileMapper userProfileMapper;
    private final UserMapper userMapper;

    public UserVerificationGuardImpl(UserProfileMapper userProfileMapper,
                                     UserMapper userMapper) {
        this.userProfileMapper = userProfileMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void requireVerifiedStudent(Long userId) {
        if (!isVerifiedStudent(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "请先完成学生认证后再进行交易操作");
        }
    }

    @Override
    public boolean isVerifiedStudent(Long userId) {
        if (userId == null) {
            return false;
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null || user.getRole() != Role.USER || user.getStatus() != UserStatus.ACTIVE) {
            return false;
        }
        UserProfileEntity profile = userProfileMapper.selectById(userId);
        return profile != null && profile.getVerifiedAt() != null;
    }
}
