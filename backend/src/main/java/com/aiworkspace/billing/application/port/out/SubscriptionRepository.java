package com.aiworkspace.billing.application.port.out;

import com.aiworkspace.billing.domain.model.Subscription;
import com.aiworkspace.billing.domain.model.SubscriptionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);

    Optional<Subscription> findActiveByUserId(UUID userId);

    Optional<Subscription> findByExternalId(String externalId);

    List<Subscription> findExpiring(SubscriptionStatus status, Instant periodEndBefore);
}
