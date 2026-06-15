package com.swapcampus.chat.vo;

import com.swapcampus.chat.entity.ChatSessionEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChatSessionResponse {

    private Long id;
    private Long productId;
    private Long buyerId;
    private Long sellerId;
    private Long peerId;
    private String peerUsername;
    private String productTitle;
    private String productImageUrl;
    private BigDecimal productPrice;
    private String productStatus;
    private String lastPreview;
    private int unreadCount;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;

    public static ChatSessionResponse from(ChatSessionEntity entity, Long currentUserId) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setId(entity.getId());
        response.setProductId(entity.getProductId());
        response.setBuyerId(entity.getBuyerId());
        response.setSellerId(entity.getSellerId());
        response.setPeerId(currentUserId.equals(entity.getBuyerId()) ? entity.getSellerId() : entity.getBuyerId());
        response.setLastMessageAt(entity.getLastMessageAt());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getPeerId() {
        return peerId;
    }

    public void setPeerId(Long peerId) {
        this.peerId = peerId;
    }

    public String getPeerUsername() {
        return peerUsername;
    }

    public void setPeerUsername(String peerUsername) {
        this.peerUsername = peerUsername;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getLastPreview() {
        return lastPreview;
    }

    public void setLastPreview(String lastPreview) {
        this.lastPreview = lastPreview;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
