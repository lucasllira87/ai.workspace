package com.aiworkspace.billing.domain.model;

public record UsageSummary(long totalTokens, int totalDocuments, long totalStorageBytes) {

    public static UsageSummary empty() {
        return new UsageSummary(0, 0, 0);
    }

    public UsageSummary add(UsageEntry entry) {
        return new UsageSummary(
                totalTokens + entry.tokenCount(),
                totalDocuments + entry.documentCount(),
                totalStorageBytes + entry.storageBytes()
        );
    }
}
