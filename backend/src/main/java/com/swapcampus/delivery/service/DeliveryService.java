package com.swapcampus.delivery.service;

import com.swapcampus.delivery.dto.DeliveryRequest;
import com.swapcampus.delivery.vo.DeliveryResponse;

public interface DeliveryService {
    DeliveryResponse reserveLocker(DeliveryRequest request);
    DeliveryResponse confirmStored(String taskNo);
    DeliveryResponse confirmPickedUp(String taskNo, String pickupCode);
    DeliveryResponse getByOrderId(Long orderId);
}