package com.swapcampus.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swapcampus.ai.adapter.AiSuggestAdapter;
import com.swapcampus.ai.adapter.AiSuggestCommand;
import com.swapcampus.ai.adapter.AiSuggestResult;
import com.swapcampus.ai.dto.AiRequest;
import com.swapcampus.ai.entity.AiEntity;
import com.swapcampus.ai.mapper.AiMapper;
import com.swapcampus.ai.service.AiService;
import com.swapcampus.ai.vo.AiResponse;
import com.swapcampus.common.security.CurrentUserContext;
import com.swapcampus.product.entity.CategoryEntity;
import com.swapcampus.product.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@Service
public class AiServiceImpl implements AiService {

    private final AiSuggestAdapter aiSuggestAdapter;
    private final AiMapper aiMapper;
    private final CategoryMapper categoryMapper;

    public AiServiceImpl(AiSuggestAdapter aiSuggestAdapter,
                         AiMapper aiMapper,
                         CategoryMapper categoryMapper) {
        this.aiSuggestAdapter = aiSuggestAdapter;
        this.aiMapper = aiMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public AiResponse suggestProduct(AiRequest request) {
        Long userId = CurrentUserContext.requireUserId();
        AiSuggestResult result = aiSuggestAdapter.suggest(new AiSuggestCommand(
                userId,
                request.getTitle(),
                request.getDescription(),
                request.getConditionLevel()));
        CategoryEntity category = matchCategory(result.categoryName());

        AiEntity log = new AiEntity();
        log.setUserId(userId);
        log.setTitle(request.getTitle().trim());
        log.setInputSummary(summary(request));
        log.setSuggestedCategoryId(category == null ? null : category.getId());
        log.setSuggestedTags(String.join(",", result.tags()));
        log.setSuggestedMinPrice(result.minPrice());
        log.setSuggestedMaxPrice(result.maxPrice());
        log.setProvider(result.provider());
        log.setCreatedAt(LocalDateTime.now());
        aiMapper.insert(log);

        AiResponse response = new AiResponse();
        response.setLogId(log.getId());
        response.setSuggestedCategoryId(log.getSuggestedCategoryId());
        response.setSuggestedCategoryName(category == null ? result.categoryName() : category.getName());
        response.setSuggestedTags(result.tags());
        response.setSuggestedMinPrice(result.minPrice());
        response.setSuggestedMaxPrice(result.maxPrice());
        response.setProvider(result.provider());
        response.setStatus("OK");
        return response;
    }

    private CategoryEntity matchCategory(String categoryName) {
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }
        return categoryMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                        .eq(CategoryEntity::getStatus, "ACTIVE")
                        .and(wrapper -> wrapper.like(CategoryEntity::getName, categoryName)
                                .or()
                                .like(CategoryEntity::getName, translateCategory(categoryName))))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private String translateCategory(String categoryName) {
        return switch (categoryName) {
            case "Textbooks" -> "教材";
            case "Digital" -> "数码";
            default -> "其他";
        };
    }

    private String summary(AiRequest request) {
        StringJoiner joiner = new StringJoiner(" | ");
        joiner.add("condition=" + (request.getConditionLevel() == null ? "" : request.getConditionLevel()));
        if (StringUtils.hasText(request.getDescription())) {
            joiner.add(request.getDescription());
        }
        return joiner.toString();
    }
}
