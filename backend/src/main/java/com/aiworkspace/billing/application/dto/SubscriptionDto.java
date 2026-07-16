package com.aiworkspace.billing.application.dto;

import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Subscription;
import com.aiworkspace.billing.domain.model.SubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionDto(UUID id, UUID userId, UUID planId, SubscriptionStatus status,
                               BillingPeriod currentPeriod, Instant canceledAt, Instant createdAt) {
    public static SubscriptionDto from(Subscription s) {
        return new SubscriptionDto(s.getId(), s.getUserId(), s.getPlanId(), s.getStatus(),
                s.getCurrentPeriod(), s.getCanceledAt(), s.getCreatedAt());
    }
}
