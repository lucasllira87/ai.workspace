package com.aiworkspace.aicore.infrastructure.provider.google;

import com.aiworkspace.aicore.application.dto.ChatMessage;
import com.aiworkspace.aicore.application.dto.ChatRequest;
import com.aiworkspace.aicore.application.dto.ChatResponse;
import com.aiworkspace.aicore.application.port.out.provider.ChatProvider;
import com.aiworkspace.aicore.domain.exception.ProviderUnavailableException;
import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;
import com.aiworkspace.aicore.domain.model.TokenUsage;
import com.aiworkspace.aicore.infrastructure.provider.ModelPricingCatalog;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GoogleChatAdapter implements ChatProvider {

    private static final ProviderId PROVIDER_ID = ProviderId.GOOGLE;
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    private static final Set<ModelId> SUPPORTED_MODELS = Set.of(
            ModelId.GEMINI_2_FLASH,
            ModelId.GEMINI_1_5_PRO
    );

    private final RestClient restClient;
    private final String apiKey;
    private final ModelPricingCatalog pricingCatalog;

    public GoogleChatAdapter(RestClient restClient, String apiKey,
                              ModelPricingCatalog pricingCatalog) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.pricingCatalog = pricingCatalog;
    }

    @Override
    public ProviderId getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public Set<ModelId> getSupportedModels() {
        return SUPPORTED_MODELS;
    }

    @Override
    public boolean supports(ModelId model) {
        return model == null || SUPPORTED_MODELS.contains(model);
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        try {
            ModelId model = request.model() != null ? request.model() : ModelId.GEMINI_2_FLASH;

            List<Map<String, Object>> contents = request.messages().stream()
                    .filter(m -> !"system".equals(m.role()))
                    .map(m -> Map.<String, Object>of(
                            "role", "user".equals(m.role()) ? "user" : "model",
                            "parts", List.of(Map.of("text", m.content()))))
                    .toList();

            Map<String, Object> body = Map.of("contents", contents);

            GeminiResponse response = restClient.post()
                    .uri(GEMINI_API_URL + "?key={key}", model.value(), apiKey)
                    .body(body)
                    .retrieve()
                    .body(GeminiResponse.class);

            String content = response != null && response.candidates() != null
                    && !response.candidates().isEmpty()
                    ? response.candidates().get(0).content().parts().get(0).text()
                    : "";

            int promptTokens = response != null && response.usageMetadata() != null
                    ? response.usageMetadata().promptTokenCount() : 0;
            int completionTokens = response != null && response.usageMetadata() != null
                    ? response.usageMetadata().candidatesTokenCount() : 0;

            TokenUsage usage = TokenUsage.of(promptTokens, completionTokens);
            EstimatedCost cost = pricingCatalog.estimateCost(model, promptTokens, completionTokens);
            long latency = System.currentTimeMillis() - start;

            return new ChatResponse(content, model, PROVIDER_ID, usage, cost, latency);

        } catch (Exception e) {
            throw new ProviderUnavailableException(PROVIDER_ID, e.getMessage(), e);
        }
    }

    // Internal DTOs for Gemini REST response
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeminiResponse(
            @JsonProperty("candidates") List<Candidate> candidates,
            @JsonProperty("usageMetadata") UsageMetadata usageMetadata) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(@JsonProperty("content") Content content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(@JsonProperty("parts") List<Part> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(@JsonProperty("text") String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record UsageMetadata(
            @JsonProperty("promptTokenCount") int promptTokenCount,
            @JsonProperty("candidatesTokenCount") int candidatesTokenCount) {}
}
