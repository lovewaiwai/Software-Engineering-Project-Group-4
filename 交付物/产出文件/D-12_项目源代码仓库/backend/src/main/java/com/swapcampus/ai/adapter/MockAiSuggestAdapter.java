package com.swapcampus.ai.adapter;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MockAiSuggestAdapter implements AiSuggestAdapter {

    @Override
    public AiSuggestResult suggest(AiSuggestCommand command) {
        String title = command.title() == null ? "" : command.title();
        if (containsAny(title, "textbook", "book", "math", "english", "教材", "高数", "英语", "考研", "课本")) {
            return new AiSuggestResult("Textbooks", List.of("课程", "教材"), BigDecimal.valueOf(20), BigDecimal.valueOf(120), "MOCK");
        }
        if (containsAny(title, "phone", "keyboard", "headphone", "tablet", "手机", "键盘", "耳机", "平板", "数码")) {
            return new AiSuggestResult("Digital", List.of("数码", "校园"), BigDecimal.valueOf(80), BigDecimal.valueOf(800), "MOCK");
        }
        return new AiSuggestResult("Other", List.of("校园", "闲置"), BigDecimal.valueOf(10), BigDecimal.valueOf(100), "MOCK");
    }

    private boolean containsAny(String source, String... keywords) {
        String normalized = source.toLowerCase();
        for (String keyword : keywords) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
