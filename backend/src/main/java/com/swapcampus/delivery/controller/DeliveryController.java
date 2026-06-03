package com.swapcampus.delivery.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.delivery.service.DeliveryService;
import com.swapcampus.delivery.vo.DeliveryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/health")
    public ApiResponse<DeliveryResponse> health() {
        return ApiResponse.ok(DeliveryResponse.placeholder(deliveryService.moduleName()));
    }
}
