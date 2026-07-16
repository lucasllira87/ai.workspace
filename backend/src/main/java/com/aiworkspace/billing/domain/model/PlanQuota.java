package com.aiworkspace.billing.domain.model;

public record PlanQuota(
        long maxTokensPerMonth,
        int maxDocuments,
        long maxStorageBytes,
        int maxEnrollments
) {
    public boolean isTokensExceeded(long usedTokens) {
        return usedTokens >= maxTokensPerMonth;
    }

    public boolean isDocumentsExceeded(int usedDocuments) {
        return usedDocuments >= maxDocuments;
    }

    public boolean isStorageExceeded(long usedBytes) {
        return usedBytes >= maxStorageBytes;
    }

    public boolean isEnrollmentsExceeded(int usedEnrollments) {
        return usedEnrollments >= maxEnrollments;
    }
}
