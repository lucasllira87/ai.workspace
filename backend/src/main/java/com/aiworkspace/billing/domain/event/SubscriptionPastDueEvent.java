package com.aiworkspace.billing.domain.event;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionPastDueEvent(UUID subscriptionId, UUID userId, UUID planId,
                                        Instant occurredAt) {}
