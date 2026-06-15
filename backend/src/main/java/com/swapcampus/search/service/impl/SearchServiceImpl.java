package com.swapcampus.search.service.impl;

import com.swapcampus.common.api.PageResponse;
import com.swapcampus.product.dto.ProductSearchRequest;
import com.swapcampus.product.service.ProductService;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.search.service.SearchService;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {

    private final ProductService productService;

    public SearchServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public PageResponse<ProductResponse> searchProducts(ProductSearchRequest request) {
        return productService.search(request);
    }
}
