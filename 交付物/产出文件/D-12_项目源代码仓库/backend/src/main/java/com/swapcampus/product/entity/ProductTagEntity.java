package com.swapcampus.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("product_tags")
public class ProductTagEntity {

    private Long productId;
    private Long tagId;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
