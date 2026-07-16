package com.aiworkspace.aicore.application.dto;

import com.aiworkspace.aicore.domain.model.EstimatedCost;
import com.aiworkspace.aicore.domain.model.ModelId;
import com.aiworkspace.aicore.domain.model.ProviderId;
import com.aiworkspace.aicore.domain.model.TokenUsage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UsageMetricEvent(
        UUID requestId,
        String module,
        String operation,
        ProviderId providerId,
        ModelId modelId,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        BigDecimal estimatedCost,
        String currency,
        long latencyMs,
        int retryCount,
        boolean success,
        String errorCode,
        String requestedBy,
        Instant requestedAt
) {
    public static UsageMetricEvent forChat(ProviderId provider, ModelId model, TokenUsage usage,
                                            EstimatedCost cost, long latencyMs, int retries,
                                            String module, String requestedBy) {
        return new UsageMetricEvent(
                UUID.randomUUID(), module, "chat", provider, model,
                usage.promptTokens(), usage.completionTokens(), usage.total(),
                cost.value(), cost.currency(), latencyMs, retries,
                true, null, requestedBy, Instant.now());
    }

    public static UsageMetricEvent forEmbedding(ProviderId provider, ModelId model, TokenUsage usage,
                                                  EstimatedCost cost, long latencyMs, int retries,
                                                  String module, String requestedBy) {
        return new UsageMetricEvent(
                UUID.randomUUID(), module, "embed", provider, model,
                usage.promptTokens(), 0, usage.total(),
                cost.value(), cost.currency(), latencyMs, retries,
                true, null, requestedBy, Instant.now());
    }

    public static UsageMetricEvent forIngestion(UUID documentId, String module, String requestedBy,
                                                  int totalTokens, long durationMs) {
        return new UsageMetricEvent(
                documentId, module, "ingest", null, null,
                totalTokens, 0, totalTokens,
                BigDecimal.ZERO, "USD", durationMs, 0,
                true, null, requestedBy, Instant.now());
    }

    public static UsageMetricEvent forError(String operation, ProviderId provider, ModelId model,
                                             long latencyMs, int retries, String errorCode,
                                             String module) {
        return new UsageMetricEvent(
                UUID.randomUUID(), module, operation, provider, model,
                0, 0, 0, BigDecimal.ZERO, "USD",
                latencyMs, retries, false, errorCode, null, Instant.now());
    }
}
