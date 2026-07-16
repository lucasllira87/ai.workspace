package com.aiworkspace.aicore.domain.event;

import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;
import com.aiworkspace.aicore.domain.model.TokenUsage;
import com.aiworkspace.shared.domain.DomainEvent;

import java.time.Instant;

public record AIRequestCompletedEvent(
        ProviderId providerId,
        ModelId modelId,
        String module,
        String requestedBy,
        String operation,
        TokenUsage usage,
        EstimatedCost cost,
        long latencyMs,
        boolean success,
        Instant occurredAt
) implements DomainEvent {}
