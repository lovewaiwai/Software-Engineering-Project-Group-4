package com.swapcampus.user.service.impl;

import com.swapcampus.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public String moduleName() {
        return "user";
    }
}
