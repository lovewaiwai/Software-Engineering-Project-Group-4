package com.swapcampus.admin.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class ProductBulkApproveRequest {

    @Size(max = 20, message = "关键词最多选择20个")
    private List<@Size(max = 30, message = "单个关键词不能超过30个字符") String> keywords;

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
