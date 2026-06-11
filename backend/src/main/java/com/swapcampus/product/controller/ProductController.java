package com.swapcampus.product.controller;

import com.swapcampus.common.api.ApiResponse;
import com.swapcampus.common.api.PageResponse;
import com.swapcampus.product.dto.ProductImageRequest;
import com.swapcampus.product.dto.ProductRequest;
import com.swapcampus.product.dto.ProductSearchRequest;
import com.swapcampus.product.service.ProductService;
import com.swapcampus.product.vo.CategoryResponse;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.product.vo.TagResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/products/health", "/product/health"})
    public ApiResponse<ProductResponse> health() {
        return ApiResponse.ok(ProductResponse.placeholder(productService.moduleName()));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> categories() {
        return ApiResponse.ok(productService.listCategories());
    }

    @GetMapping("/tags")
    public ApiResponse<List<TagResponse>> tags() {
        return ApiResponse.ok(productService.listTags());
    }

    @PostMapping("/products")
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @PutMapping("/products/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ApiResponse.ok(productService.update(id, request));
    }

    @PostMapping("/products/{id}/submit-review")
    public ApiResponse<ProductResponse> submitForReview(@PathVariable Long id) {
        return ApiResponse.ok(productService.submitForReview(id));
    }

    @PostMapping("/products/{id}/offline")
    public ApiResponse<ProductResponse> offline(@PathVariable Long id) {
        return ApiResponse.ok(productService.offline(id));
    }

    @PostMapping("/products/{id}/relist")
    public ApiResponse<ProductResponse> relist(@PathVariable Long id) {
        return ApiResponse.ok(productService.relist(id));
    }

    @GetMapping("/products/mine")
    public ApiResponse<PageResponse<ProductResponse>> mine(@ModelAttribute ProductSearchRequest request) {
        return ApiResponse.ok(productService.listMine(request));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(productService.detail(id));
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<ProductResponse>> search(@ModelAttribute ProductSearchRequest request) {
        return ApiResponse.ok(productService.search(request));
    }

    @PostMapping("/products/{id}/images")
    public ApiResponse<ProductResponse> addImage(@PathVariable Long id, @Valid @RequestBody ProductImageRequest request) {
        return ApiResponse.ok(productService.addImage(id, request));
    }

    @PostMapping("/products/{id}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long id) {
        productService.favorite(id);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/products/{id}/favorite")
    public ApiResponse<Void> unfavorite(@PathVariable Long id) {
        productService.unfavorite(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/products/{id}/view")
    public ApiResponse<Void> recordView(@PathVariable Long id) {
        productService.recordView(id);
        return ApiResponse.ok(null);
    }
}
