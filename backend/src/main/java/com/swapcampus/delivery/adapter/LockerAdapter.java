package com.swapcampus.delivery.adapter;

public interface LockerAdapter {

    LockerReserveResult reserveBox(LockerReserveCommand command);

    LockerStoreResult confirmStored(String lockerTaskNo);

    LockerPickupResult confirmPickedUp(String lockerTaskNo, String pickupCode);
}
