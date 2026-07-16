package com.aiworkspace.aicore.application.dto;

import com.aiworkspace.aicore.domain.model.ModelId;

import java.util.List;
import java.util.Map;

public record EmbeddingRequest(
        ModelId model,
        List<String> texts,
        Map<String, Object> metadata
) {
    public static EmbeddingRequest of(ModelId model, List<String> texts) {
        return new EmbeddingRequest(model, texts, Map.of());
    }
}
