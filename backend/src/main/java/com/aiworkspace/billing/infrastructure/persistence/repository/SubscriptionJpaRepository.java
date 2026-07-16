package com.aiworkspace.billing.infrastructure.persistence.repository;

import com.aiworkspace.billing.infrastructure.persistence.entity.SubscriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {

    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.userId = :userId " +
           "AND s.status IN ('TRIAL', 'ACTIVE', 'PAST_DUE') ORDER BY s.createdAt DESC")
    Optional<SubscriptionJpaEntity> findActiveByUserId(@Param("userId") UUID userId);

    Optional<SubscriptionJpaEntity> findByExternalId(String externalId);

    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.status = :status AND s.periodEnd <= :before")
    List<SubscriptionJpaEntity> findExpiring(@Param("status") String status,
                                              @Param("before") Instant before);
}
