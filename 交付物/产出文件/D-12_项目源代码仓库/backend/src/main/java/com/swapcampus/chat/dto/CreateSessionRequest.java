package com.swapcampus.chat.dto;

import jakarta.validation.constraints.NotNull;

public class CreateSessionRequest {

    private Long productId;

    @NotNull
    private Long sellerId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
}
