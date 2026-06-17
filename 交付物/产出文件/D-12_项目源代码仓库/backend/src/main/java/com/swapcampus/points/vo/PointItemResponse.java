package com.swapcampus.points.vo;

public class PointItemResponse {

    private String itemCode;
    private String itemName;
    private Integer costPoints;
    private String description;

    public PointItemResponse() {
    }

    public PointItemResponse(String itemCode, String itemName, Integer costPoints, String description) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.costPoints = costPoints;
        this.description = description;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getCostPoints() {
        return costPoints;
    }

    public void setCostPoints(Integer costPoints) {
        this.costPoints = costPoints;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}