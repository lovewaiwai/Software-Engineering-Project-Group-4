package com.swapcampus.payment.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.payment.dto.PaymentRequest;
import com.swapcampus.payment.service.PaymentService;
import com.swapcampus.payment.vo.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.ok(paymentService.createPayment(request));
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> queryPayment(@PathVariable Long paymentId) {
        return ApiResponse.ok(paymentService.queryPayment(paymentId));
    }

    @PostMapping("/mock-callback/{providerTradeNo}")
    public ApiResponse<PaymentResponse> mockCallback(@PathVariable String providerTradeNo) {
        return ApiResponse.ok(paymentService.mockCallback(providerTradeNo));
    }

    @PostMapping("/refund/{orderId}")
    public ApiResponse<PaymentResponse> refund(@PathVariable Long orderId) {
        return ApiResponse.ok(paymentService.refund(orderId));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<PaymentResponse> getByOrderId(@PathVariable Long orderId) {
        return ApiResponse.ok(paymentService.getByOrderId(orderId));
    }
}