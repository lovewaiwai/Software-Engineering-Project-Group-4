package com.swapcampus.points.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PointRedeemRequest {

    @NotBlank(message = "兑换项编码不能为空")
    @Size(max = 50, message = "兑换项编码不能超过50个字符")
    private String itemCode;

    /**
     * 商品曝光加速时选中的商品 ID，仅 PRODUCT_BOOST 生效。
     */
    private Long productId;

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
