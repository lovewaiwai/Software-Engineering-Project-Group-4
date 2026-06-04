package com.swapcampus.payment.adapter;

import java.math.BigDecimal;

public record PaymentCreateCommand(Long orderId, String orderNo, BigDecimal amount, String subject) {
}
