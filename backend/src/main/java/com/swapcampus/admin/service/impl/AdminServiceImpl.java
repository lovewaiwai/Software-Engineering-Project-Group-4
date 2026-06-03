package com.swapcampus.admin.service.impl;

import com.swapcampus.admin.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Override
    public String moduleName() {
        return "admin";
    }
}
