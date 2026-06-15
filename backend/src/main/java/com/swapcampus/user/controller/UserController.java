package com.swapcampus.user.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.api.PageResponse;
import com.swapcampus.user.dto.UserProfileUpdateRequest;
import com.swapcampus.user.dto.UserStudentVerifyRequest;
import com.swapcampus.user.service.UserService;
import com.swapcampus.user.vo.CreditRecordResponse;
import com.swapcampus.user.vo.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(userService.getCurrentUser());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(userService.getUserById(id));
    }

    @PutMapping("/me/profile")
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return ApiResponse.ok(userService.updateCurrentUserProfile(request));
    }

    @PostMapping("/me/verify-student")
    public ApiResponse<UserResponse> verifyStudent(@Valid @RequestBody UserStudentVerifyRequest request) {
        return ApiResponse.ok(userService.verifyStudent(request));
    }

    @GetMapping("/me/credit-records")
    public ApiResponse<PageResponse<CreditRecordResponse>> creditRecords(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(userService.getCreditRecords(page, pageSize));
    }
}
