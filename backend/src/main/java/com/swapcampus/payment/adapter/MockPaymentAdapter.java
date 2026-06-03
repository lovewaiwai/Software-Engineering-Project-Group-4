package com.swapcampus.payment.adapter;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentAdapter implements PaymentAdapter {

    @Override
    public PaymentCreateResult createPayment(PaymentCreateCommand command) {
        String tradeNo = "MOCK_PAY_" + UUID.randomUUID();
        return new PaymentCreateResult("MOCK", tradeNo, command.amount(), "CREATED", "mockpay://" + tradeNo);
    }

    @Override
    public PaymentQueryResult queryPayment(String providerTradeNo) {
        return new PaymentQueryResult(providerTradeNo, "SUCCESS");
    }

    @Override
    public RefundResult refund(RefundCommand command) {
        return new RefundResult(command.providerTradeNo(), command.amount(), "REFUNDED");
    }
}
