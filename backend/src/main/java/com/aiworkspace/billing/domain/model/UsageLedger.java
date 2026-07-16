package com.aiworkspace.billing.domain.model;

import com.aiworkspace.billing.domain.event.UsageLimitExceededEvent;
import com.aiworkspace.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UsageLedger extends AggregateRoot {

    private final UUID id;
    private final UUID userId;
    private final BillingPeriod period;
    private final List<UsageEntry> entries;
    private UsageSummary totals;
    private final Instant createdAt;
    private Instant updatedAt;

    private UsageLedger(UUID id, UUID userId, BillingPeriod period,
                         List<UsageEntry> entries, UsageSummary totals,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.period = period;
        this.entries = new ArrayList<>(entries);
        this.totals = totals;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UsageLedger create(UUID id, UUID userId, BillingPeriod period) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(period, "period");
        Instant now = Instant.now();
        return new UsageLedger(id, userId, period, List.of(), UsageSummary.empty(), now, now);
    }

    public static UsageLedger reconstitute(UUID id, UUID userId, BillingPeriod period,
                                            List<UsageEntry> entries, UsageSummary totals,
                                            Instant createdAt, Instant updatedAt) {
        return new UsageLedger(id, userId, period, entries, totals, createdAt, updatedAt);
    }

    public void record(UsageEntry entry, PlanQuota quota) {
        entries.add(entry);
        totals = totals.add(entry);
        updatedAt = Instant.now();

        if (quota.isTokensExceeded(totals.totalTokens())) {
            registerEvent(new UsageLimitExceededEvent(id, userId, "tokens",
                    totals.totalTokens(), quota.maxTokensPerMonth(), updatedAt));
        }
    }

    public boolean isOverTokenLimit(PlanQuota quota) {
        return quota.isTokensExceeded(totals.totalTokens());
    }

    public boolean isOverDocumentLimit(PlanQuota quota) {
        return quota.isDocumentsExceeded(totals.totalDocuments());
    }

    public boolean isOverStorageLimit(PlanQuota quota) {
        return quota.isStorageExceeded(totals.totalStorageBytes());
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public BillingPeriod getPeriod() { return period; }
    public List<UsageEntry> getEntries() { return Collections.unmodifiableList(entries); }
    public UsageSummary getTotals() { return totals; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
