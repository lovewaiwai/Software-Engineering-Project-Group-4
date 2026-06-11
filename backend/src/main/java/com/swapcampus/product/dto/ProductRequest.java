package com.swapcampus.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {

    private Long categoryId;

    @Size(max = 120)
    private String title;

    @Size(max = 4000)
    private String description;

    @DecimalMin("0.01")
    private BigDecimal price;

    @DecimalMin("0.01")
    private BigDecimal originalPrice;

    @Size(max = 20)
    private String conditionLevel;

    @Size(max = 50)
    private String campus;

    private String status;
    private List<String> tradeModes;
    private List<String> imageUrls;
    private List<Long> tagIds;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getConditionLevel() {
        return conditionLevel;
    }

    public void setConditionLevel(String conditionLevel) {
        this.conditionLevel = conditionLevel;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public List<String> getTradeModes() {
        return tradeModes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTradeModes(List<String> tradeModes) {
        this.tradeModes = tradeModes;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}
