package com.aiworkspace.billing.domain.event;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionExpiredEvent(UUID subscriptionId, UUID userId,
                                        UUID planId, Instant occurredAt) {}
