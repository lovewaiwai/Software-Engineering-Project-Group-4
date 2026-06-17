package com.swapcampus.user.service.impl;

import com.swapcampus.common.enums.UserStatus;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.service.UserAccountGuard;
import org.springframework.stereotype.Service;

@Service
public class UserAccountGuardImpl implements UserAccountGuard {

    private final UserMapper userMapper;

    public UserAccountGuardImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserEntity requireActiveUser(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (UserStatus.BANNED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, BANNED_MESSAGE);
        }
        return user;
    }

    @Override
    public boolean isBanned(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        return user != null && UserStatus.BANNED.equals(user.getStatus());
    }
}
