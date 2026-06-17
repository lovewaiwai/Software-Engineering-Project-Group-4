package com.swapcampus.review.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.security.CurrentUserPrincipal;
import com.swapcampus.review.dto.ReviewRequest;
import com.swapcampus.review.service.ReviewService;
import com.swapcampus.review.vo.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ApiResponse<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CurrentUserPrincipal principal) {
        return ApiResponse.ok(reviewService.createReview(request, principal.getUserId()));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<ReviewResponse>> getReviewsByUser(@PathVariable Long userId) {
        return ApiResponse.ok(reviewService.getReviewsByUser(userId));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<ReviewResponse>> getReviewsByOrder(@PathVariable Long orderId) {
        return ApiResponse.ok(reviewService.getReviewsByOrder(orderId));
    }
}