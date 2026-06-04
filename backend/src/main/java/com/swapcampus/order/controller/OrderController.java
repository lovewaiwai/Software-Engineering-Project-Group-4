package com.swapcampus.order.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.order.service.OrderService;
import com.swapcampus.order.vo.OrderResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/health")
    public ApiResponse<OrderResponse> health() {
        return ApiResponse.ok(OrderResponse.placeholder(orderService.moduleName()));
    }
}
