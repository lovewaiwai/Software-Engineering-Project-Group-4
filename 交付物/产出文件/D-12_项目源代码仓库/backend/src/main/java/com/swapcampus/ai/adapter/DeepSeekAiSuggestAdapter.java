package com.swapcampus.ai.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Primary
@Component
public class DeepSeekAiSuggestAdapter implements AiSuggestAdapter {

    private static final String SYSTEM_PROMPT = """
            你是校园二手交易平台 SwapCampus 的商品定价助手。
            请根据商品标题、描述、成色，为校园二手交易场景输出 JSON。
            必须只输出 JSON，格式：
            {
              "categoryName": "数码设备/图书资料/生活用品/运动户外/其他",
              "tags": ["标签1", "标签2"],
              "minPrice": 20.00,
              "maxPrice": 120.00
            }
            价格单位为人民币元，范围要符合大学校园二手交易常见成交价。
            """;

    private final DeepSeekClient deepSeekClient;
    private final MockAiSuggestAdapter fallback;

    public DeepSeekAiSuggestAdapter(DeepSeekClient deepSeekClient, MockAiSuggestAdapter fallback) {
        this.deepSeekClient = deepSeekClient;
        this.fallback = fallback;
    }

    @Override
    public AiSuggestResult suggest(AiSuggestCommand command) {
        return deepSeekClient.chatJson(SYSTEM_PROMPT, userPrompt(command), 400)
                .flatMap(this::parse)
                .orElseGet(() -> fallback.suggest(command));
    }

    private String userPrompt(AiSuggestCommand command) {
        return """
                请输出 json。
                商品标题：%s
                商品描述：%s
                商品成色：%s
                """.formatted(
                safe(command.title()),
                safe(command.description()),
                safe(command.conditionLevel()));
    }

    private java.util.Optional<AiSuggestResult> parse(String content) {
        try {
            JsonNode root = deepSeekClient.objectMapper().readTree(content);
            String categoryName = text(root, "categoryName", "其他");
            List<String> tags = tags(root.get("tags"));
            BigDecimal minPrice = decimal(root, "minPrice", BigDecimal.TEN);
            BigDecimal maxPrice = decimal(root, "maxPrice", minPrice.multiply(BigDecimal.valueOf(2)));
            if (maxPrice.compareTo(minPrice) < 0) {
                BigDecimal tmp = minPrice;
                minPrice = maxPrice;
                maxPrice = tmp;
            }
            return java.util.Optional.of(new AiSuggestResult(categoryName, tags, minPrice, maxPrice, "DEEPSEEK"));
        } catch (Exception ex) {
            return java.util.Optional.empty();
        }
    }

    private List<String> tags(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of("校园", "闲置");
        }
        List<String> result = new ArrayList<>();
        node.forEach(item -> {
            String value = item.asText("");
            if (StringUtils.hasText(value) && result.size() < 5) {
                result.add(value.trim());
            }
        });
        return result.isEmpty() ? List.of("校园", "闲置") : result;
    }

    private String text(JsonNode root, String field, String fallbackValue) {
        String value = root.path(field).asText("");
        return StringUtils.hasText(value) ? value.trim() : fallbackValue;
    }

    private BigDecimal decimal(JsonNode root, String field, BigDecimal fallbackValue) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return fallbackValue;
        }
        try {
            return new BigDecimal(node.asText()).max(BigDecimal.ZERO);
        } catch (NumberFormatException ex) {
            return fallbackValue;
        }
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
