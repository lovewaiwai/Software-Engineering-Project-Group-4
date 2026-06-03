package com.swapcampus.ai.adapter;

import java.math.BigDecimal;
import java.util.List;

public record AiSuggestResult(String categoryName, List<String> tags, BigDecimal minPrice, BigDecimal maxPrice, String provider) {
}
