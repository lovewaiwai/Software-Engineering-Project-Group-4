package com.swapcampus.search.service;

import com.swapcampus.common.api.PageResponse;
import com.swapcampus.product.dto.ProductSearchRequest;
import com.swapcampus.product.vo.ProductResponse;

public interface SearchService {

    PageResponse<ProductResponse> searchProducts(ProductSearchRequest request);
}
