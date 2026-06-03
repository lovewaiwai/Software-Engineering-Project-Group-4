package com.swapcampus.delivery.adapter;

public record LockerReserveCommand(Long orderId, Long stationId, String size) {
}
