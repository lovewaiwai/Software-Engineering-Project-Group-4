package com.swapcampus.user.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.user.service.UserService;
import com.swapcampus.user.vo.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    public ApiResponse<UserResponse> health() {
        return ApiResponse.ok(UserResponse.placeholder(userService.moduleName()));
    }
}
