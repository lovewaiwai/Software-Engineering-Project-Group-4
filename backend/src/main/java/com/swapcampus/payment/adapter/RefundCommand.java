package com.swapcampus.payment.adapter;

import java.math.BigDecimal;

public record RefundCommand(String providerTradeNo, BigDecimal amount, String reason) {
}
