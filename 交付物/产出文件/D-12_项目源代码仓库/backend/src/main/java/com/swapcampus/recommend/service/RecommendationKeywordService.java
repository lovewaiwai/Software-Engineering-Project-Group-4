package com.swapcampus.recommend.service;

import com.swapcampus.ai.adapter.DeepSeekClient;
import com.swapcampus.product.entity.BrowseRecordEntity;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.BrowseRecordMapper;
import com.swapcampus.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RecommendationKeywordService {

    private static final String SYSTEM_PROMPT = """
            你是校园二手交易平台 SwapCampus 的个性化推荐助手。
            请根据用户最近浏览的商品标题和描述，提炼适合检索其他商品的关键词。
            必须只输出 JSON，格式：
            {"keywords":["关键词1","关键词2","关键词3"]}
            关键词应短小，优先是品类、用途、品牌、课程或商品类型，不要输出用户隐私信息。
            """;

    private final BrowseRecordMapper browseRecordMapper;
    private final ProductMapper productMapper;
    private final DeepSeekClient deepSeekClient;

    public RecommendationKeywordService(BrowseRecordMapper browseRecordMapper,
                                        ProductMapper productMapper,
                                        DeepSeekClient deepSeekClient) {
        this.browseRecordMapper = browseRecordMapper;
        this.productMapper = productMapper;
        this.deepSeekClient = deepSeekClient;
    }

    public List<String> keywordsForUser(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }
        List<ProductEntity> historyProducts = recentHistoryProducts(userId);
        if (historyProducts.isEmpty()) {
            return List.of();
        }
        List<String> aiKeywords = deepSeekClient.chatJson(SYSTEM_PROMPT, userPrompt(historyProducts), 300)
                .flatMap(this::parseKeywords)
                .orElse(List.of());
        if (!aiKeywords.isEmpty()) {
            return aiKeywords.stream().limit(limit).toList();
        }
        return fallbackKeywords(historyProducts, limit);
    }

    private List<ProductEntity> recentHistoryProducts(Long userId) {
        return browseRecordMapper.selectLatestByUser(userId, 0, 12)
                .stream()
                .map(BrowseRecordEntity::getProductId)
                .map(productMapper::selectById)
                .filter(product -> product != null && !Boolean.TRUE.equals(product.getDeleted()))
                .toList();
    }

    private String userPrompt(List<ProductEntity> products) {
        StringBuilder builder = new StringBuilder("请输出 json。最近浏览商品：\n");
        for (ProductEntity product : products) {
            builder.append("- 标题：").append(safe(product.getTitle()))
                    .append("；描述：").append(safe(product.getDescription()))
                    .append('\n');
        }
        return builder.toString();
    }

    private Optional<List<String>> parseKeywords(String content) {
        try {
            JsonNode root = deepSeekClient.objectMapper().readTree(content);
            JsonNode keywords = root.get("keywords");
            if (keywords == null || !keywords.isArray()) {
                return Optional.empty();
            }
            Set<String> result = new LinkedHashSet<>();
            keywords.forEach(item -> {
                String value = item.asText("");
                if (StringUtils.hasText(value)) {
                    result.add(value.trim());
                }
            });
            return result.isEmpty() ? Optional.empty() : Optional.of(new ArrayList<>(result));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private List<String> fallbackKeywords(List<ProductEntity> products, int limit) {
        Set<String> result = new LinkedHashSet<>();
        for (ProductEntity product : products) {
            addTokens(result, product.getTitle());
            if (result.size() >= limit) {
                break;
            }
        }
        return result.stream().limit(limit).toList();
    }

    private void addTokens(Set<String> result, String source) {
        if (!StringUtils.hasText(source)) {
            return;
        }
        for (String token : source.trim().split("[\\s,，、。|/]+")) {
            if (StringUtils.hasText(token) && token.length() >= 2 && result.size() < 8) {
                result.add(token.trim());
            }
        }
    }

    private String safe(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim().length() > 160 ? value.trim().substring(0, 160) : value.trim();
    }
}
