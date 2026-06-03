package com.swapcampus.auth.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.auth.service.AuthService;
import com.swapcampus.auth.vo.AuthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/health")
    public ApiResponse<AuthResponse> health() {
        return ApiResponse.ok(AuthResponse.placeholder(authService.moduleName()));
    }
}
