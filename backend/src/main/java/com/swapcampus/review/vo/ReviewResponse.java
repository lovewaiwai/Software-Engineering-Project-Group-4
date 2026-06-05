package com.swapcampus.review.vo;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private Long orderId;
    private Long reviewerId;
    private Long revieweeId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponse from(com.swapcampus.review.entity.ReviewEntity e) {
        ReviewResponse r = new ReviewResponse();
        r.setId(e.getId());
        r.setOrderId(e.getOrderId());
        r.setReviewerId(e.getReviewerId());
        r.setRevieweeId(e.getRevieweeId());
        r.setRating(e.getRating());
        r.setContent(e.getContent());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public Long getRevieweeId() { return revieweeId; }
    public void setRevieweeId(Long revieweeId) { this.revieweeId = revieweeId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}