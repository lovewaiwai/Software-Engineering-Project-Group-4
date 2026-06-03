package com.swapcampus.auth.service.impl;

import com.swapcampus.auth.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public String moduleName() {
        return "auth";
    }
}
