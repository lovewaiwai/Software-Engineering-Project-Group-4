package com.swapcampus.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("products")
public class ProductEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sellerId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String conditionLevel;
    private String campus;
    private String tradeModes;
    private String status;
    private Integer viewCount;
    private Integer favoriteCount;
    private String auditReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime boostedUntil;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

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

    public String getTradeModes() { return tradeModes; }
    public void setTradeModes(String tradeModes) { this.tradeModes = tradeModes; }

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

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public Boolean getIsDeleted() { return deleted; }
    public void setIsDeleted(Boolean isDeleted) { this.deleted = isDeleted; }
}
