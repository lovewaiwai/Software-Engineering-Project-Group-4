package com.swapcampus.ai.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AiResponse {

    private Long logId;
    private Long suggestedCategoryId;
    private String suggestedCategoryName;
    private List<String> suggestedTags = new ArrayList<>();
    private BigDecimal suggestedMinPrice;
    private BigDecimal suggestedMaxPrice;
    private String provider;
    private String status;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getSuggestedCategoryId() {
        return suggestedCategoryId;
    }

    public void setSuggestedCategoryId(Long suggestedCategoryId) {
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public String getSuggestedCategoryName() {
        return suggestedCategoryName;
    }

    public void setSuggestedCategoryName(String suggestedCategoryName) {
        this.suggestedCategoryName = suggestedCategoryName;
    }

    public List<String> getSuggestedTags() {
        return suggestedTags;
    }

    public void setSuggestedTags(List<String> suggestedTags) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
