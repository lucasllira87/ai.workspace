package com.aiworkspace.audit.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DashboardStats(UUID userId,
                              DocumentStats documents,
                              LearningStats learning,
                              BillingStats billing,
                              List<AuditEventSummary> recentActivity,
                              Instant generatedAt) {

    public static DashboardStats of(UUID userId, DocumentStats documents, LearningStats learning,
                                     BillingStats billing, List<AuditEventSummary> recentActivity) {
        return new DashboardStats(userId, documents, learning, billing,
                List.copyOf(recentActivity), Instant.now());
    }
}
