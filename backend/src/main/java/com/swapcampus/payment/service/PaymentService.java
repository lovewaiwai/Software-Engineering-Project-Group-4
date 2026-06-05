package com.swapcampus.payment.service;

import com.swapcampus.payment.dto.PaymentRequest;
import com.swapcampus.payment.vo.PaymentResponse;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse queryPayment(Long paymentId);
    PaymentResponse mockCallback(String providerTradeNo);
    PaymentResponse refund(Long orderId);
}