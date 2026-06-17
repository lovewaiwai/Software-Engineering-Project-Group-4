package com.swapcampus.ai.adapter;

public record AiSuggestCommand(Long userId, String title, String description, String conditionLevel) {
}
