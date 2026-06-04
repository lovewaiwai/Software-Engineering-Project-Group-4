package com.swapcampus.payment.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.payment.service.PaymentService;
import com.swapcampus.payment.vo.PaymentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/health")
    public ApiResponse<PaymentResponse> health() {
        return ApiResponse.ok(PaymentResponse.placeholder(paymentService.moduleName()));
    }
}
