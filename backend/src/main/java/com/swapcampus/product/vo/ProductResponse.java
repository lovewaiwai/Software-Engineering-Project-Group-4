package com.swapcampus.product.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductResponse {

    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String conditionLevel;
    private String campus;
    private List<String> tradeModes = new ArrayList<>();
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private String auditReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime boostedUntil;
    private List<String> imageUrls = new ArrayList<>();
    private List<Long> tagIds = new ArrayList<>();
    private List<String> tagNames = new ArrayList<>();
    private Boolean favorited;
    private String recommendReason;
    private Integer sellerCreditScore;
    private String sellerCreditLevel;
    private String module;

    public static ProductResponse placeholder(String module) {
        ProductResponse response = new ProductResponse();
        response.setModule(module);
        response.setStatus("TODO");
        return response;
    }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public String getConditionLevel() { return conditionLevel; }
    public void setConditionLevel(String conditionLevel) { this.conditionLevel = conditionLevel; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public List<String> getTradeModes() { return tradeModes; }
    public void setTradeModes(List<String> tradeModes) { this.tradeModes = tradeModes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }

    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getBoostedUntil() { return boostedUntil; }
    public void setBoostedUntil(LocalDateTime boostedUntil) { this.boostedUntil = boostedUntil; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }

    public List<String> getTagNames() { return tagNames; }
    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }

    public Boolean getFavorited() { return favorited; }
    public void setFavorited(Boolean favorited) { this.favorited = favorited; }

    public String getRecommendReason() { return recommendReason; }
    public void setRecommendReason(String recommendReason) { this.recommendReason = recommendReason; }

    public Integer getSellerCreditScore() { return sellerCreditScore; }
    public void setSellerCreditScore(Integer sellerCreditScore) { this.sellerCreditScore = sellerCreditScore; }

    public String getSellerCreditLevel() { return sellerCreditLevel; }
    public void setSellerCreditLevel(String sellerCreditLevel) { this.sellerCreditLevel = sellerCreditLevel; }
}
