package com.swapcampus.recommend.service;

import com.swapcampus.product.vo.ProductResponse;

import java.util.List;

public interface RecommendService {

    List<ProductResponse> recommendations(int limit);
}
