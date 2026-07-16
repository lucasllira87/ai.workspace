package com.aiworkspace.aicore.application.command;

import com.aiworkspace.aicore.domain.model.ModelId;

import java.util.List;

public record GenerateEmbeddingCommand(
        List<String> texts,
        ModelId preferredModel,
        String module,
        String requestedBy
) {
    public static GenerateEmbeddingCommand of(List<String> texts, String module, String requestedBy) {
        return new GenerateEmbeddingCommand(texts, null, module, requestedBy);
    }
}
