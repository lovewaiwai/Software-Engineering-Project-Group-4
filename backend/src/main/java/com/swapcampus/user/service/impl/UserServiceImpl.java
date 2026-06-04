package com.swapcampus.user.service.impl;

import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.entity.UserProfileEntity;
import com.swapcampus.user.mapper.UserMapper;
import com.swapcampus.user.mapper.UserProfileMapper;
import com.swapcampus.user.service.UserService;
import com.swapcampus.user.vo.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;

    public UserServiceImpl(UserMapper userMapper, UserProfileMapper userProfileMapper) {
        this.userMapper = userMapper;
        this.userProfileMapper = userProfileMapper;
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
}
