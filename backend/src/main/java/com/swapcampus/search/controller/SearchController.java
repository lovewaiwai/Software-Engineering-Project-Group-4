package com.swapcampus.search.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.api.PageResponse;
import com.swapcampus.product.dto.ProductSearchRequest;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.search.service.SearchService;
import com.swapcampus.search.vo.SearchResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/health")
    public ApiResponse<SearchResponse> health() {
        return ApiResponse.ok(SearchResponse.placeholder(searchService.moduleName()));
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(@ModelAttribute ProductSearchRequest request) {
        return ApiResponse.ok(searchService.searchProducts(request));
    }
}
