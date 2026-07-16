package com.aiworkspace.billing.domain.event;

import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Money;

import java.time.Instant;
import java.util.UUID;

public record InvoiceGeneratedEvent(UUID invoiceId, UUID userId, UUID subscriptionId,
                                     Money totalAmount, BillingPeriod period, Instant occurredAt) {}
