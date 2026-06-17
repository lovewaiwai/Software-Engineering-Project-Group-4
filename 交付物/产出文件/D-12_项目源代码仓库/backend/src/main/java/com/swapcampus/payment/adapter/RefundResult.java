package com.swapcampus.payment.adapter;

import java.math.BigDecimal;

public record RefundResult(String providerTradeNo, BigDecimal amount, String status) {
}
