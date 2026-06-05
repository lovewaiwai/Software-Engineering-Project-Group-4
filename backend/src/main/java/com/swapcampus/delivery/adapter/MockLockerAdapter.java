package com.swapcampus.delivery.adapter;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
public class MockLockerAdapter implements LockerAdapter {

    private final SecureRandom random = new SecureRandom();

    @Override
    public LockerReserveResult reserveBox(LockerReserveCommand command) {
        String taskNo = "LK" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        String pickupCode = String.valueOf(100000 + random.nextInt(900000));
        return new LockerReserveResult(taskNo, "Campus North Gate Locker", "M-" + random.nextInt(40), pickupCode, "locker-deposit://" + taskNo);
    }

    @Override
    public LockerStoreResult confirmStored(String lockerTaskNo) {
        return new LockerStoreResult(lockerTaskNo, "STORED");
    }

    @Override
    public LockerPickupResult confirmPickedUp(String lockerTaskNo, String pickupCode) {
        return new LockerPickupResult(lockerTaskNo, "PICKED_UP");
    }
}
