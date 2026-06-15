package com.swapcampus.recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.swapcampus.recommend.service.RecommendationKeywordService;
import com.swapcampus.user.entity.UserEntity;
import com.swapcampus.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RecommendServiceImpl implements RecommendService {

    private final ProductMapper productMapper;
    private final BrowseRecordMapper browseRecordMapper;
    private final ProductServiceImpl productService;
    private final UserMapper userMapper;
    private final RecommendationKeywordService recommendationKeywordService;

    public RecommendServiceImpl(ProductMapper productMapper,
                                BrowseRecordMapper browseRecordMapper,
                                ProductServiceImpl productService,
                                UserMapper userMapper,
                                RecommendationKeywordService recommendationKeywordService) {
        this.productMapper = productMapper;
        this.browseRecordMapper = browseRecordMapper;
        this.productService = productService;
        this.userMapper = userMapper;
        this.recommendationKeywordService = recommendationKeywordService;
    }

    @Override
    public List<ProductResponse> recommendations(int limit) {
        int size = Math.max(1, Math.min(limit, 50));
        Long userId = CurrentUserContext.currentUserId().orElse(null);
        Long preferredCategory = preferredCategory(userId);
        List<String> aiKeywords = recommendationKeywordService.keywordsForUser(userId, 8);
        List<ProductEntity> keywordCandidates = keywordCandidates(aiKeywords, size * 4);
        List<ProductEntity> hotCandidates = hotCandidates(size * 4);

        Comparator<ProductEntity> byRecommendScore =
                Comparator.comparingInt(product -> score(product, preferredCategory, aiKeywords));
        Map<Long, ProductEntity> candidateMap = new LinkedHashMap<>();
        keywordCandidates.forEach(product -> candidateMap.put(product.getId(), product));
        hotCandidates.forEach(product -> candidateMap.putIfAbsent(product.getId(), product));

        Map<Long, ProductResponse> deduplicated = new LinkedHashMap<>();
        candidateMap.values().stream()
                .sorted(byRecommendScore.reversed())
                .limit(size)
                .forEach(product -> {
                    ProductResponse response = productService.toResponse(product, userId);
                    response.setRecommendReason(reason(product, preferredCategory, aiKeywords));
                    deduplicated.put(product.getId(), response);
                });
        return List.copyOf(deduplicated.values());
    }

    private List<ProductEntity> hotCandidates(int limit) {
        return productMapper.selectPage(new Page<>(1, Math.max(1, limit)),
                new LambdaQueryWrapper<ProductEntity>()
                        .eq(ProductEntity::getStatus, ProductStatus.ACTIVE.name())
                        .orderByDesc(ProductEntity::getFavoriteCount)
                        .orderByDesc(ProductEntity::getViewCount)
                        .orderByDesc(ProductEntity::getCreatedAt)).getRecords();
    }

    private List<ProductEntity> keywordCandidates(List<String> keywords, int limit) {
        List<String> usableKeywords = keywords.stream()
                .filter(keyword -> keyword != null && !keyword.isBlank())
                .limit(8)
                .toList();
        if (usableKeywords.isEmpty()) {
            return List.of();
        }
        QueryWrapper<ProductEntity> wrapper = new QueryWrapper<ProductEntity>()
                .eq("status", ProductStatus.ACTIVE.name())
                .and(group -> {
                    for (int i = 0; i < usableKeywords.size(); i++) {
                        String keyword = usableKeywords.get(i);
                        if (i > 0) {
                            group.or();
                        }
                        group.like("title", keyword).or().like("description", keyword);
                    }
                })
                .orderByDesc("favorite_count")
                .orderByDesc("view_count")
                .orderByDesc("created_at");
        return productMapper.selectPage(new Page<>(1, Math.max(1, limit)), wrapper).getRecords();
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

    private int score(ProductEntity product, Long preferredCategory, List<String> aiKeywords) {
        int score = 0;
        if (preferredCategory != null && preferredCategory.equals(product.getCategoryId())) {
            score += 35;
        }
        score += keywordScore(product, aiKeywords);
        score += Math.min(20, product.getFavoriteCount() == null ? 0 : product.getFavoriteCount());
        score += Math.min(20, product.getViewCount() == null ? 0 : product.getViewCount() / 5);
        score += creditScoreBonus(product.getSellerId());
        score += product.getCreatedAt() == null ? 0 : 10;
        score += ThreadLocalRandom.current().nextInt(0, 9);
        return score;
    }

    private String reason(ProductEntity product, Long preferredCategory, List<String> aiKeywords) {
        if (keywordScore(product, aiKeywords) > 0) {
            return "根据你最近浏览的关键词推荐";
        }
        if (preferredCategory != null && preferredCategory.equals(product.getCategoryId())) {
            return "根据你最近浏览的分类推荐";
        }
        String creditLevel = sellerCreditLevel(product.getSellerId());
        if ("极好".equals(creditLevel) || "优秀".equals(creditLevel)) {
            return "卖家信用" + creditLevel + "，优先推荐";
        }
        if (product.getFavoriteCount() != null && product.getFavoriteCount() > 0) {
            return "近期收藏热度较高";
        }
        return "最新上架商品";
    }

    private int keywordScore(ProductEntity product, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }
        String haystack = ((product.getTitle() == null ? "" : product.getTitle()) + " "
                + (product.getDescription() == null ? "" : product.getDescription())).toLowerCase();
        int score = 0;
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isBlank() && haystack.contains(keyword.trim().toLowerCase())) {
                score += 28;
            }
        }
        return Math.min(score, 70);
    }

    private int creditScoreBonus(Long sellerId) {
        UserEntity seller = sellerId == null ? null : userMapper.selectById(sellerId);
        int creditScore = seller == null || seller.getCreditScore() == null ? 60 : seller.getCreditScore();
        if (creditScore >= 95) {
            return 30;
        }
        if (creditScore >= 85) {
            return 22;
        }
        if (creditScore >= 60) {
            return 10;
        }
        return -20;
    }

    private String sellerCreditLevel(Long sellerId) {
        UserEntity seller = sellerId == null ? null : userMapper.selectById(sellerId);
        int creditScore = seller == null || seller.getCreditScore() == null ? 60 : seller.getCreditScore();
        return productService.creditLevel(creditScore);
    }
}
