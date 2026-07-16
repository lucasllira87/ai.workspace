package com.aiworkspace.aicore.application.dto;

import com.aiworkspace.aicore.domain.model.ModelId;

import java.util.List;
import java.util.Map;

public record ChatRequest(
        ModelId model,
        List<ChatMessage> messages,
        Double temperature,
        Integer maxTokens,
        Map<String, Object> metadata
) {
    public static ChatRequest of(ModelId model, List<ChatMessage> messages) {
        return new ChatRequest(model, messages, null, null, Map.of());
    }

    public static ChatRequest withMetadata(ModelId model, List<ChatMessage> messages,
                                            Map<String, Object> metadata) {
        return new ChatRequest(model, messages, null, null, metadata);
    }
}
