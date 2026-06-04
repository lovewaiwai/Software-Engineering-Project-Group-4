package com.swapcampus.auth.service;

import com.swapcampus.auth.dto.LoginRequest;
import com.swapcampus.auth.dto.RegisterRequest;
import com.swapcampus.auth.vo.AuthResponse;

public interface AuthService {

    String moduleName();

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout();
}
