package com.swapcampus.user.service;

import com.swapcampus.user.vo.UserResponse;

public interface UserService {

    String moduleName();

    UserResponse getCurrentUser();

    UserResponse getUserById(Long userId);
}
