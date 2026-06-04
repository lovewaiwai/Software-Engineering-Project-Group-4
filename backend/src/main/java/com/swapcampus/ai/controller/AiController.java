package com.swapcampus.ai.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.ai.service.AiService;
import com.swapcampus.ai.vo.AiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/health")
    public ApiResponse<AiResponse> health() {
        return ApiResponse.ok(AiResponse.placeholder(aiService.moduleName()));
    }
}
