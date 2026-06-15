package com.swapcampus.recommend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapcampus.ai.adapter.DeepSeekClient;
import com.swapcampus.product.entity.BrowseRecordEntity;
import com.swapcampus.product.entity.ProductEntity;
import com.swapcampus.product.mapper.BrowseRecordMapper;
import com.swapcampus.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationKeywordServiceTest {

    @Test
    void keywordsForUserPrefersDeepSeekKeywords() {
        BrowseRecordMapper browseRecordMapper = mock(BrowseRecordMapper.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);
        RecommendationKeywordService service = new RecommendationKeywordService(browseRecordMapper, productMapper, deepSeekClient);
        when(browseRecordMapper.selectLatestByUser(7L, 0, 12)).thenReturn(List.of(record(10L)));
        when(productMapper.selectById(10L)).thenReturn(product("蓝牙耳机", "降噪，九成新"));
        when(deepSeekClient.chatJson(anyString(), anyString(), anyInt())).thenReturn(Optional.of("""
                {"keywords":["蓝牙","耳机","降噪"]}
                """));
        when(deepSeekClient.objectMapper()).thenReturn(new ObjectMapper());

        assertEquals(List.of("蓝牙", "耳机"), service.keywordsForUser(7L, 2));
    }

    @Test
    void keywordsForUserFallsBackToTitleTokens() {
        BrowseRecordMapper browseRecordMapper = mock(BrowseRecordMapper.class);
        ProductMapper productMapper = mock(ProductMapper.class);
        DeepSeekClient deepSeekClient = mock(DeepSeekClient.class);
        RecommendationKeywordService service = new RecommendationKeywordService(browseRecordMapper, productMapper, deepSeekClient);
        when(browseRecordMapper.selectLatestByUser(7L, 0, 12)).thenReturn(List.of(record(10L)));
        when(productMapper.selectById(10L)).thenReturn(product("考研 数学 教材", "全套"));
        when(deepSeekClient.chatJson(anyString(), anyString(), anyInt())).thenReturn(Optional.empty());

        assertEquals(List.of("考研", "数学"), service.keywordsForUser(7L, 2));
    }

    private BrowseRecordEntity record(Long productId) {
        BrowseRecordEntity record = new BrowseRecordEntity();
        record.setProductId(productId);
        return record;
    }

    private ProductEntity product(String title, String description) {
        ProductEntity product = new ProductEntity();
        product.setTitle(title);
        product.setDescription(description);
        product.setDeleted(false);
        return product;
    }
}
