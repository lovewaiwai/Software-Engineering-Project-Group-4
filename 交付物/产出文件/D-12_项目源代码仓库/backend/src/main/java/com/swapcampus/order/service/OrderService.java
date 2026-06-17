package com.swapcampus.order.service;

import com.swapcampus.order.dto.OrderRequest;
import com.swapcampus.order.vo.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request, Long buyerId);
    OrderResponse getOrder(Long orderId, Long currentUserId);
    List<OrderResponse> listMyOrders(Long currentUserId, String role);
    OrderResponse cancelOrder(Long orderId, Long currentUserId);
    OrderResponse confirmComplete(Long orderId, Long currentUserId);
    OrderResponse sellerConfirm(Long orderId, Long sellerId);
    OrderResponse sellerReject(Long orderId, Long sellerId);
}