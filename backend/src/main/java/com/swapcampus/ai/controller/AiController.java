package com.swapcampus.ai.controller;

import com.swapcampus.ai.dto.AiRequest;
import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.ai.service.AiService;
import com.swapcampus.ai.vo.AiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/product-suggestions")
    public ApiResponse<AiResponse> productSuggestions(@Valid @RequestBody AiRequest request) {
        return ApiResponse.ok(aiService.suggestProduct(request));
    }
}
