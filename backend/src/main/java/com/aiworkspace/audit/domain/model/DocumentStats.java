package com.aiworkspace.audit.domain.model;

public record DocumentStats(long totalCount, long indexedCount, long totalStorageBytes) {

    public static DocumentStats empty() {
        return new DocumentStats(0, 0, 0);
    }
}
