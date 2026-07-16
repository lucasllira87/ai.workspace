package com.aiworkspace.billing.domain.event;

import com.aiworkspace.billing.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(UUID invoiceId, UUID userId, UUID subscriptionId,
                                  Money amount, Instant occurredAt) {}
