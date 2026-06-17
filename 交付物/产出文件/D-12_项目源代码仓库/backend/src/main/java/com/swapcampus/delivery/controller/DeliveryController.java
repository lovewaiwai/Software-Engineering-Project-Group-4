package com.swapcampus.delivery.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.delivery.dto.DeliveryRequest;
import com.swapcampus.delivery.service.DeliveryService;
import com.swapcampus.delivery.vo.DeliveryResponse;
import com.swapcampus.delivery.vo.LockerStationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/reserve")
    public ApiResponse<DeliveryResponse> reserveLocker(@Valid @RequestBody DeliveryRequest request) {
        return ApiResponse.ok(deliveryService.reserveLocker(request));
    }

    @GetMapping("/stations")
    public ApiResponse<List<LockerStationResponse>> listStations() {
        return ApiResponse.ok(deliveryService.listStations());
    }

    @PostMapping("/{taskNo}/stored")
    public ApiResponse<DeliveryResponse> confirmStored(@PathVariable String taskNo) {
        return ApiResponse.ok(deliveryService.confirmStored(taskNo));
    }

    @PostMapping("/{taskNo}/pickup")
    public ApiResponse<DeliveryResponse> confirmPickedUp(
            @PathVariable String taskNo,
            @RequestParam String pickupCode) {
        return ApiResponse.ok(deliveryService.confirmPickedUp(taskNo, pickupCode));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<DeliveryResponse> getByOrderId(@PathVariable Long orderId) {
        return ApiResponse.ok(deliveryService.getByOrderId(orderId));
    }
}
