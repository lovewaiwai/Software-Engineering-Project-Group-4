package com.swapcampus.payment.adapter;

public interface PaymentAdapter {

    PaymentCreateResult createPayment(PaymentCreateCommand command);

    PaymentQueryResult queryPayment(String providerTradeNo);

    RefundResult refund(RefundCommand command);
}
