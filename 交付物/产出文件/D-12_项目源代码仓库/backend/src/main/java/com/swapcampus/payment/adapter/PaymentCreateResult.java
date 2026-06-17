package com.swapcampus.payment.adapter;

import java.math.BigDecimal;

public record PaymentCreateResult(String provider, String providerTradeNo, BigDecimal amount, String status, String payUrl) {
}
