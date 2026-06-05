package com.swapcampus.recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapcampus.common.enums.ProductStatus;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.product.entity.BrowseRecordEntity;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.BrowseRecordMapper;
import com.swapcampus.product.mapper.ProductMapper;
import com.swapcampus.product.service.impl.ProductServiceImpl;
import com.swapcampus.product.vo.ProductResponse;
import com.swapcampus.recommend.service.RecommendService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendServiceImpl implements RecommendService {

    private final ProductMapper productMapper;
    private final BrowseRecordMapper browseRecordMapper;
    private final ProductServiceImpl productService;

    public RecommendServiceImpl(ProductMapper productMapper,
                                BrowseRecordMapper browseRecordMapper,
                                ProductServiceImpl productService) {
        this.productMapper = productMapper;
        this.browseRecordMapper = browseRecordMapper;
        this.productService = productService;
    }

    @Override
    public String moduleName() {
        return "recommend";
    }

    @Override
    public List<ProductResponse> recommendations(int limit) {
        int size = Math.max(1, Math.min(limit, 50));
        Long userId = CurrentUserContext.currentUserId().orElse(null);
        Long preferredCategory = preferredCategory(userId);
        List<ProductEntity> candidates = productMapper.selectPage(new Page<>(1, size * 3L),
                new LambdaQueryWrapper<ProductEntity>()
                        .eq(ProductEntity::getStatus, ProductStatus.ACTIVE.name())
                        .orderByDesc(ProductEntity::getFavoriteCount)
                        .orderByDesc(ProductEntity::getViewCount)
                        .orderByDesc(ProductEntity::getCreatedAt)).getRecords();

        Comparator<ProductEntity> byRecommendScore =
                Comparator.comparingInt(product -> score(product, preferredCategory));
        Map<Long, ProductResponse> deduplicated = new LinkedHashMap<>();
        candidates.stream()
                .sorted(byRecommendScore.reversed())
                .limit(size)
                .forEach(product -> {
                    ProductResponse response = productService.toResponse(product, userId);
                    response.setRecommendReason(reason(product, preferredCategory));
                    deduplicated.put(product.getId(), response);
                });
        return List.copyOf(deduplicated.values());
    }

    private Long preferredCategory(Long userId) {
        if (userId == null) {
            return null;
        }
        return browseRecordMapper.selectList(new LambdaQueryWrapper<BrowseRecordEntity>()
                        .eq(BrowseRecordEntity::getUserId, userId)
                        .isNotNull(BrowseRecordEntity::getCategoryId)
                        .orderByDesc(BrowseRecordEntity::getCreatedAt)
                        .last("OFFSET 0 ROWS FETCH NEXT 30 ROWS ONLY"))
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        BrowseRecordEntity::getCategoryId,
                        java.util.stream.Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private int score(ProductEntity product, Long preferredCategory) {
        int score = 0;
        if (preferredCategory != null && preferredCategory.equals(product.getCategoryId())) {
            score += 35;
        }
        score += Math.min(20, product.getFavoriteCount() == null ? 0 : product.getFavoriteCount());
        score += Math.min(20, product.getViewCount() == null ? 0 : product.getViewCount() / 5);
        score += product.getCreatedAt() == null ? 0 : 10;
        return score;
    }

    private String reason(ProductEntity product, Long preferredCategory) {
        if (preferredCategory != null && preferredCategory.equals(product.getCategoryId())) {
            return "根据你最近浏览的分类推荐";
        }
        if (product.getFavoriteCount() != null && product.getFavoriteCount() > 0) {
            return "近期收藏热度较高";
        }
        return "最新上架商品";
    }
}
