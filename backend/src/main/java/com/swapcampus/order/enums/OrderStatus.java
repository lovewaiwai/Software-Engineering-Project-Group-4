package com.swapcampus.order.enums;

/**
 * @deprecated 请使用 com.swapcampus.common.enums.OrderStatus
 */
public enum OrderStatus {
    CREATED,
    SELLER_CONFIRMED,
    SELLER_REJECTED,  // 新增
    PAYMENT_PENDING,
    PAID,
    DELIVERY_PENDING,
    RECEIVED,
    COMPLETED,
    CANCELLED,
    REFUNDING,
    REFUNDED,
    DISPUTED,
    CLOSED
}