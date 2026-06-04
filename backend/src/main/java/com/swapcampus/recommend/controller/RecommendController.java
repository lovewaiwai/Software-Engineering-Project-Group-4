package com.swapcampus.recommend.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.recommend.service.RecommendService;
import com.swapcampus.recommend.vo.RecommendResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/health")
    public ApiResponse<RecommendResponse> health() {
        return ApiResponse.ok(RecommendResponse.placeholder(recommendService.moduleName()));
    }
}
