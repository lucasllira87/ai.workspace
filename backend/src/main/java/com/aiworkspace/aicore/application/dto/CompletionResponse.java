package com.aiworkspace.aicore.application.dto;

import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;
import com.aiworkspace.aicore.domain.model.TokenUsage;

public record CompletionResponse(
        String text,
        ModelId model,
        ProviderId provider,
        TokenUsage usage,
        EstimatedCost estimatedCost,
        long latencyMs
) {}
