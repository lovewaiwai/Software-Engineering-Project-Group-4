package com.swapcampus.delivery.vo;

import com.swapcampus.delivery.entity.LockerStationEntity;

public class LockerStationResponse {

    private Long id;
    private String name;
    private String location;
    private String status;
    private long emptyBoxes;
    private long reservedBoxes;
    private long occupiedBoxes;

    public static LockerStationResponse from(LockerStationEntity station) {
        LockerStationResponse response = new LockerStationResponse();
        response.setId(station.getId());
        response.setName(station.getName());
        response.setLocation(station.getLocation());
        response.setStatus(station.getStatus());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getEmptyBoxes() { return emptyBoxes; }
    public void setEmptyBoxes(long emptyBoxes) { this.emptyBoxes = emptyBoxes; }
    public long getReservedBoxes() { return reservedBoxes; }
    public void setReservedBoxes(long reservedBoxes) { this.reservedBoxes = reservedBoxes; }
    public long getOccupiedBoxes() { return occupiedBoxes; }
    public void setOccupiedBoxes(long occupiedBoxes) { this.occupiedBoxes = occupiedBoxes; }
}
