package com.swapcampus.product.vo;

import java.time.LocalDateTime;

public class BrowseHistoryResponse {

    private ProductResponse product;
    private LocalDateTime viewedAt;

    public ProductResponse getProduct() {
        return product;
    }

    public void setProduct(ProductResponse product) {
        this.product = product;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
