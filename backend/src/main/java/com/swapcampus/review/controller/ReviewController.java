package com.swapcampus.review.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.review.service.ReviewService;
import com.swapcampus.review.vo.ReviewResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/health")
    public ApiResponse<ReviewResponse> health() {
        return ApiResponse.ok(ReviewResponse.placeholder(reviewService.moduleName()));
    }
}
