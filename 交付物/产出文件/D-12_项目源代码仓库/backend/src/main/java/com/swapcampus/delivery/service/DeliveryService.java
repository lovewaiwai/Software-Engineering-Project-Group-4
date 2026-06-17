package com.swapcampus.delivery.service;

import com.swapcampus.delivery.dto.DeliveryRequest;
import com.swapcampus.delivery.vo.DeliveryResponse;
import com.swapcampus.delivery.vo.LockerStationResponse;
import com.swapcampus.delivery.vo.LockerTaskResponse;

import java.util.List;

public interface DeliveryService {
    DeliveryResponse reserveLocker(DeliveryRequest request);
    DeliveryResponse confirmStored(String taskNo);
    DeliveryResponse confirmPickedUp(String taskNo, String pickupCode);
    DeliveryResponse getByOrderId(Long orderId);
    List<LockerStationResponse> listStations();
    List<LockerTaskResponse> listTasks();
}
