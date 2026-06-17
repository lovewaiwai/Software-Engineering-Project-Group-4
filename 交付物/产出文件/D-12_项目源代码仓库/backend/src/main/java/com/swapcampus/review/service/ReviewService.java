package com.swapcampus.review.service;

import com.swapcampus.review.dto.ReviewRequest;
import com.swapcampus.review.vo.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request, Long reviewerId);
    List<ReviewResponse> getReviewsByUser(Long userId);
    List<ReviewResponse> getReviewsByOrder(Long orderId);
}