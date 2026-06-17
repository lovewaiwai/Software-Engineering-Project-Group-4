package com.swapcampus.admin.dto;

import jakarta.validation.constraints.Size;

public class ProductReviewRequest {

    @Size(max = 300, message = "审核原因不能超过300个字符")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
