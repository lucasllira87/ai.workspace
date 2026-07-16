package com.aiworkspace.billing.domain.event;

import java.time.Instant;
import java.util.UUID;

public record UsageLimitExceededEvent(UUID ledgerId, UUID userId, String limitType,
                                       long currentValue, long maxValue, Instant occurredAt) {}
