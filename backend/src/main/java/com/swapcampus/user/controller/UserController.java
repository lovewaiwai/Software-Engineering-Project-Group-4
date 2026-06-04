package com.swapcampus.user.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.user.dto.VerifyStudentRequest;
import com.swapcampus.user.service.UserService;
import com.swapcampus.user.vo.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    public ApiResponse<UserResponse> health() {
        return ApiResponse.ok(UserResponse.placeholder(userService.moduleName()));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(userService.getCurrentUser());
    }

    @PostMapping("/me/verify-student")
    public ApiResponse<UserResponse> verifyStudent(@Valid @RequestBody VerifyStudentRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        return ApiResponse.ok(userService.verifyStudent(userId, request));
    }
}
