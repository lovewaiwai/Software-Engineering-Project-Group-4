package com.swapcampus.order.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.security.CurrentUserPrincipal;
import com.swapcampus.order.dto.OrderRequest;
import com.swapcampus.order.service.OrderService;
import com.swapcampus.order.vo.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.ok(orderService.createOrder(request, principal.getUserId()));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.ok(orderService.getOrder(orderId, principal.getUserId()));
    }

    @GetMapping("/my")
    public ApiResponse<List<OrderResponse>> listMyOrders(
            @RequestParam(defaultValue = "buyer") String role,
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.ok(orderService.listMyOrders(principal.getUserId(), role));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.ok(orderService.cancelOrder(orderId, principal.getUserId()));
    }

    @PostMapping("/{orderId}/complete")
    public ApiResponse<OrderResponse> confirmComplete(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.ok(orderService.confirmComplete(orderId, principal.getUserId()));
    }
    @PostMapping("/{orderId}/seller-confirm")
    public ApiResponse<OrderResponse> sellerConfirm(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.ok(orderService.sellerConfirm(orderId, principal.getUserId()));
    }

    @PostMapping("/{orderId}/seller-reject")
    public ApiResponse<OrderResponse> sellerReject(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.ok(orderService.sellerReject(orderId, principal.getUserId()));
    }
}