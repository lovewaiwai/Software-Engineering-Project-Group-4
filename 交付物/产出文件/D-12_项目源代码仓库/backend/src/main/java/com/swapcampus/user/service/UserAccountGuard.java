package com.swapcampus.user.service;

import com.swapcampus.user.entity.UserEntity;

public interface UserAccountGuard {

    String BANNED_MESSAGE = "账号已被封禁，如有疑问请联系平台审核员";

    UserEntity requireActiveUser(Long userId);

    boolean isBanned(Long userId);
}
