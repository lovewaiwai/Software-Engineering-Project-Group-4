package com.swapcampus.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("ai_suggestion_logs")
public class AiEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String inputSummary;
    private Long suggestedCategoryId;
    private String suggestedTags;
    private BigDecimal suggestedMinPrice;
    private BigDecimal suggestedMaxPrice;
    private String provider;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInputSummary() {
        return inputSummary;
    }

    public void setInputSummary(String inputSummary) {
        this.inputSummary = inputSummary;
    }

    public Long getSuggestedCategoryId() {
        return suggestedCategoryId;
    }

    public void setSuggestedCategoryId(Long suggestedCategoryId) {
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public String getSuggestedTags() {
        return suggestedTags;
    }

    public void setSuggestedTags(String suggestedTags) {
        this.suggestedTags = suggestedTags;
    }

    public BigDecimal getSuggestedMinPrice() {
        return suggestedMinPrice;
    }

    public void setSuggestedMinPrice(BigDecimal suggestedMinPrice) {
        this.suggestedMinPrice = suggestedMinPrice;
    }

    public BigDecimal getSuggestedMaxPrice() {
        return suggestedMaxPrice;
    }

    public void setSuggestedMaxPrice(BigDecimal suggestedMaxPrice) {
        this.suggestedMaxPrice = suggestedMaxPrice;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
