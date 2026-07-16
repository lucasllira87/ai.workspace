package com.aiworkspace.billing.domain.model;

import com.aiworkspace.billing.domain.event.SubscriptionActivatedEvent;
import com.aiworkspace.billing.domain.event.SubscriptionCanceledEvent;
import com.aiworkspace.billing.domain.event.SubscriptionExpiredEvent;
import com.aiworkspace.billing.domain.event.SubscriptionPastDueEvent;
import com.aiworkspace.billing.domain.event.SubscriptionRenewedEvent;
import com.aiworkspace.billing.domain.event.TrialStartedEvent;
import com.aiworkspace.shared.domain.AggregateRoot;
import com.aiworkspace.shared.exception.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Subscription extends AggregateRoot {

    private static final int TRIAL_DAYS = 14;

    private final UUID id;
    private final UUID userId;
    private UUID planId;
    private SubscriptionStatus status;
    private BillingPeriod currentPeriod;
    private String externalId;
    private Instant canceledAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Subscription(UUID id, UUID userId, UUID planId, SubscriptionStatus status,
                          BillingPeriod currentPeriod, String externalId,
                          Instant canceledAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.planId = planId;
        this.status = status;
        this.currentPeriod = currentPeriod;
        this.externalId = externalId;
        this.canceledAt = canceledAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Subscription startTrial(UUID id, UUID userId, UUID freePlanId) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(freePlanId, "freePlanId");

        Instant now = Instant.now();
        BillingPeriod trialPeriod = BillingPeriod.trialPeriod(TRIAL_DAYS);
        Subscription subscription = new Subscription(id, userId, freePlanId,
                SubscriptionStatus.TRIAL, trialPeriod, null, null, now, now);
        subscription.registerEvent(new TrialStartedEvent(id, userId, freePlanId, trialPeriod.endAt(), now));
        return subscription;
    }

    public static Subscription reconstitute(UUID id, UUID userId, UUID planId, SubscriptionStatus status,
                                             BillingPeriod currentPeriod, String externalId,
                                             Instant canceledAt, Instant createdAt, Instant updatedAt) {
        return new Subscription(id, userId, planId, status, currentPeriod,
                externalId, canceledAt, createdAt, updatedAt);
    }

    public void activate(UUID newPlanId, String externalId, BillingPeriod period) {
        if (status == SubscriptionStatus.CANCELED || status == SubscriptionStatus.EXPIRED) {
            throw new DomainException("Cannot activate a canceled or expired subscription");
        }
        this.planId = newPlanId;
        this.externalId = externalId;
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriod = period;
        this.updatedAt = Instant.now();
        registerEvent(new SubscriptionActivatedEvent(id, userId, newPlanId, period, updatedAt));
    }

    public void renew(BillingPeriod nextPeriod) {
        if (status != SubscriptionStatus.ACTIVE && status != SubscriptionStatus.PAST_DUE) {
            throw new DomainException("Cannot renew a subscription with status: " + status);
        }
        this.currentPeriod = nextPeriod;
        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = Instant.now();
        registerEvent(new SubscriptionRenewedEvent(id, userId, planId, nextPeriod, updatedAt));
    }

    public void markPastDue() {
        if (status != SubscriptionStatus.ACTIVE) {
            throw new DomainException("Only ACTIVE subscriptions can become PAST_DUE");
        }
        this.status = SubscriptionStatus.PAST_DUE;
        this.updatedAt = Instant.now();
        registerEvent(new SubscriptionPastDueEvent(id, userId, planId, updatedAt));
    }

    public void cancel() {
        if (status == SubscriptionStatus.CANCELED || status == SubscriptionStatus.EXPIRED) {
            throw new DomainException("Subscription is already canceled or expired");
        }
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = Instant.now();
        this.updatedAt = this.canceledAt;
        registerEvent(new SubscriptionCanceledEvent(id, userId, planId, canceledAt));
    }

    public void expire() {
        if (status == SubscriptionStatus.CANCELED || status == SubscriptionStatus.EXPIRED) return;
        this.status = SubscriptionStatus.EXPIRED;
        this.updatedAt = Instant.now();
        registerEvent(new SubscriptionExpiredEvent(id, userId, planId, updatedAt));
    }

    public boolean isAccessible() {
        return status == SubscriptionStatus.TRIAL
                || status == SubscriptionStatus.ACTIVE
                || status == SubscriptionStatus.PAST_DUE;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getPlanId() { return planId; }
    public SubscriptionStatus getStatus() { return status; }
    public BillingPeriod getCurrentPeriod() { return currentPeriod; }
    public String getExternalId() { return externalId; }
    public Instant getCanceledAt() { return canceledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
