package com.swapcampus.ai.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapcampus.ai.config.DeepSeekProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DeepSeekClient {

    private final DeepSeekProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    public DeepSeekClient(DeepSeekProperties properties,
                          RestClient.Builder restClientBuilder,
                          ObjectMapper objectMapper) {
        this.properties = properties;
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
    }

    public boolean available() {
        return properties.isEnabled() && StringUtils.hasText(properties.getApiKey());
    }

    public Optional<String> chatJson(String systemPrompt, String userPrompt, int maxTokens) {
        if (!available()) {
            return Optional.empty();
        }
        try {
            Map<String, Object> request = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "response_format", Map.of("type", "json_object"),
                    "max_tokens", maxTokens,
                    "temperature", 0.2,
                    "thinking", Map.of("type", "disabled")
            );
            DeepSeekChatResponse response = restClient()
                    .post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(properties.getApiKey()))
                    .body(request)
                    .retrieve()
                    .body(DeepSeekChatResponse.class);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return Optional.empty();
            }
            String content = response.choices().get(0).message().content();
            return StringUtils.hasText(content) ? Optional.of(content) : Optional.empty();
        } catch (RestClientException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    private RestClient restClient() {
        return restClientBuilder
                .baseUrl(normalizedBaseUrl())
                .requestFactory(requestFactory())
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds()));
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    private String normalizedBaseUrl() {
        String baseUrl = StringUtils.hasText(properties.getBaseUrl())
                ? properties.getBaseUrl().trim()
                : "https://api.deepseek.com";
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeepSeekChatResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String content) {
    }
}
