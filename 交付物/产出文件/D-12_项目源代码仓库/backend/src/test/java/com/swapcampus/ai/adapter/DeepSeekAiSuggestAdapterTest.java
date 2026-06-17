package com.swapcampus.ai.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeepSeekAiSuggestAdapterTest {

    @Test
    void suggestParsesDeepSeekJsonResult() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        MockAiSuggestAdapter fallback = mock(MockAiSuggestAdapter.class);
        when(client.chatJson(anyString(), anyString(), anyInt())).thenReturn(Optional.of("""
                {"categoryName":"数码设备","tags":["耳机","蓝牙"],"minPrice":80,"maxPrice":180}
                """));
        when(client.objectMapper()).thenReturn(new ObjectMapper());
        DeepSeekAiSuggestAdapter adapter = new DeepSeekAiSuggestAdapter(client, fallback);

        AiSuggestResult result = adapter.suggest(new AiSuggestCommand(1L, "蓝牙耳机", "九成新", "LIKE_NEW"));

        assertEquals("DEEPSEEK", result.provider());
        assertEquals("数码设备", result.categoryName());
        assertEquals(List.of("耳机", "蓝牙"), result.tags());
        assertEquals(new BigDecimal("80"), result.minPrice());
        assertEquals(new BigDecimal("180"), result.maxPrice());
    }

    @Test
    void suggestFallsBackWhenDeepSeekUnavailable() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        MockAiSuggestAdapter fallback = mock(MockAiSuggestAdapter.class);
        AiSuggestCommand command = new AiSuggestCommand(1L, "教材", "高数", "GOOD");
        AiSuggestResult fallbackResult = new AiSuggestResult("Textbooks", List.of("教材"), BigDecimal.TEN, BigDecimal.valueOf(50), "MOCK");
        when(client.chatJson(anyString(), anyString(), anyInt())).thenReturn(Optional.empty());
        when(fallback.suggest(command)).thenReturn(fallbackResult);
        DeepSeekAiSuggestAdapter adapter = new DeepSeekAiSuggestAdapter(client, fallback);

        assertEquals(fallbackResult, adapter.suggest(command));
    }
}
