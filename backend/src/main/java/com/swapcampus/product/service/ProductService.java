package com.swapcampus.product.service;

import com.swapcampus.common.api.PageResponse;
import com.swapcampus.product.dto.ProductImageRequest;
import com.swapcampus.product.dto.ProductRequest;
import com.swapcampus.product.dto.ProductSearchRequest;
import com.swapcampus.product.vo.BrowseHistoryResponse;
import com.swapcampus.product.vo.CategoryResponse;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.product.vo.TagResponse;

import java.util.List;

public interface ProductService {

    List<CategoryResponse> listCategories();

    List<TagResponse> listTags();

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse detail(Long id);

    PageResponse<ProductResponse> search(ProductSearchRequest request);

    PageResponse<ProductResponse> listMine(ProductSearchRequest request);

    PageResponse<BrowseHistoryResponse> listBrowseHistory(long page, long pageSize);

    ProductResponse submitForReview(Long id);

    ProductResponse offline(Long id);

    ProductResponse relist(Long id);

    ProductResponse addImage(Long id, ProductImageRequest request);

    void favorite(Long id);

    void unfavorite(Long id);

    void recordView(Long id);
}
