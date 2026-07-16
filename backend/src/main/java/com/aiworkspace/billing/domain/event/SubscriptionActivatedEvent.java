package com.aiworkspace.billing.domain.event;

import com.aiworkspace.billing.domain.model.BillingPeriod;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionActivatedEvent(UUID subscriptionId, UUID userId, UUID planId,
                                          BillingPeriod period, Instant occurredAt) {}
