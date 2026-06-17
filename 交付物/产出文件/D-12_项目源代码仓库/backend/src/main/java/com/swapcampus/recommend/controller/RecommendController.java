package com.swapcampus.recommend.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.recommend.service.RecommendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/recommendations")
    public ApiResponse<List<ProductResponse>> recommendations(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(recommendService.recommendations(limit));
    }
}
