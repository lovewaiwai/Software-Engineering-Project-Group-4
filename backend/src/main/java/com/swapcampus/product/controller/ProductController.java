package com.swapcampus.product.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.product.service.ProductService;
import com.swapcampus.product.vo.ProductResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/health")
    public ApiResponse<ProductResponse> health() {
        return ApiResponse.ok(ProductResponse.placeholder(productService.moduleName()));
    }
}
