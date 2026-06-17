package com.swapcampus.admin.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.enums.Role;
import com.swapcampus.common.exception.BusinessException;
import com.swapcampus.common.exception.ErrorCode;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.delivery.service.DeliveryService;
import com.swapcampus.delivery.vo.LockerStationResponse;
import com.swapcampus.delivery.vo.LockerTaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lockers")
public class AdminLockerController {

    private final DeliveryService deliveryService;

    public AdminLockerController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/stations")
    public ApiResponse<List<LockerStationResponse>> listStations() {
        requireSystemAdmin();
        return ApiResponse.ok(deliveryService.listStations());
    }

    @GetMapping("/tasks")
    public ApiResponse<List<LockerTaskResponse>> listTasks() {
        requireSystemAdmin();
        return ApiResponse.ok(deliveryService.listTasks());
    }

    private void requireSystemAdmin() {
        Role role = CurrentUserContext.currentRole()
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));
        if (role != Role.SYS_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "需要系统管理员权限");
        }
    }
}
