package com.swapcampus.admin.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.admin.service.AdminService;
import com.swapcampus.admin.vo.AdminResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/health")
    public ApiResponse<AdminResponse> health() {
        return ApiResponse.ok(AdminResponse.placeholder(adminService.moduleName()));
    }
}
