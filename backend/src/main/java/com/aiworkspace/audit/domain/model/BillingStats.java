package com.aiworkspace.audit.domain.model;

public record BillingStats(String planName, String subscriptionStatus,
                            long tokensUsedThisPeriod, long tokenQuota,
                            long daysUntilRenewal) {

    public static BillingStats unavailable() {
        return new BillingStats("UNKNOWN", "UNKNOWN", 0, 0, 0);
    }

    public double usagePercent() {
        if (tokenQuota == 0) return 0.0;
        return (double) tokensUsedThisPeriod / tokenQuota * 100.0;
    }
}
