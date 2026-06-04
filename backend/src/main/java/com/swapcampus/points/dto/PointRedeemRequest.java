package com.swapcampus.points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PointRedeemRequest {

    @NotBlank(message = "兑换项编码不能为空")
    @Size(max = 50, message = "兑换项编码不能超过50个字符")
    private String itemCode;

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
}