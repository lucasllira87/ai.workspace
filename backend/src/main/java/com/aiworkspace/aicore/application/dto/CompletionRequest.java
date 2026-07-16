package com.aiworkspace.aicore.application.dto;

import com.aiworkspace.aicore.domain.model.ModelId;

public record CompletionRequest(
        ModelId model,
        String prompt,
        Double temperature,
        Integer maxTokens
) {}
