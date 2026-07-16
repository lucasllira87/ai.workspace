package com.aiworkspace.billing.domain.model;

import java.time.Instant;
import java.util.UUID;

public record UsageEntry(
        UUID id,
        String module,
        String operation,
        long tokenCount,
        int documentCount,
        long storageBytes,
        Instant recordedAt
) {
    public static UsageEntry of(String module, String operation, long tokenCount,
                                 int documentCount, long storageBytes) {
        return new UsageEntry(UUID.randomUUID(), module, operation,
                tokenCount, documentCount, storageBytes, Instant.now());
    }
}
