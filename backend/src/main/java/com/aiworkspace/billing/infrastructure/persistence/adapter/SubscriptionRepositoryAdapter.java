package com.aiworkspace.billing.infrastructure.persistence.adapter;

import com.aiworkspace.billing.application.port.out.SubscriptionRepository;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Subscription;
import com.aiworkspace.billing.domain.model.SubscriptionStatus;
import com.aiworkspace.billing.infrastructure.persistence.entity.SubscriptionJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.repository.SubscriptionJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;

    public SubscriptionRepositoryAdapter(SubscriptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Subscription save(Subscription subscription) {
        jpaRepository.save(toEntity(subscription));
        return subscription;
    }

    @Override
    public Optional<Subscription> findActiveByUserId(UUID userId) {
        return jpaRepository.findActiveByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<Subscription> findByExternalId(String externalId) {
        return jpaRepository.findByExternalId(externalId).map(this::toDomain);
    }

    @Override
    public List<Subscription> findExpiring(SubscriptionStatus status, Instant periodEndBefore) {
        return jpaRepository.findExpiring(status.name(), periodEndBefore).stream()
                .map(this::toDomain).toList();
    }

    private SubscriptionJpaEntity toEntity(Subscription s) {
        SubscriptionJpaEntity entity = new SubscriptionJpaEntity();
        entity.setId(s.getId());
        entity.setUserId(s.getUserId());
        entity.setPlanId(s.getPlanId());
        entity.setStatus(s.getStatus().name());
        entity.setExternalId(s.getExternalId());
        entity.setPeriodStart(s.getCurrentPeriod().startAt());
        entity.setPeriodEnd(s.getCurrentPeriod().endAt());
        entity.setCanceledAt(s.getCanceledAt());
        entity.setCreatedAt(s.getCreatedAt());
        entity.setUpdatedAt(s.getUpdatedAt());
        return entity;
    }

    private Subscription toDomain(SubscriptionJpaEntity entity) {
        return Subscription.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getPlanId(),
                SubscriptionStatus.valueOf(entity.getStatus()),
                new BillingPeriod(entity.getPeriodStart(), entity.getPeriodEnd()),
                entity.getExternalId(),
                entity.getCanceledAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
