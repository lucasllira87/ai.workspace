package com.aiworkspace.billing.infrastructure.persistence.repository;

import com.aiworkspace.billing.infrastructure.persistence.entity.UsageLedgerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UsageLedgerJpaRepository extends JpaRepository<UsageLedgerJpaEntity, UUID> {

    @Query("SELECT l FROM UsageLedgerJpaEntity l WHERE l.userId = :userId " +
           "AND l.periodStart = :periodStart")
    Optional<UsageLedgerJpaEntity> findByUserIdAndPeriodStart(@Param("userId") UUID userId,
                                                               @Param("periodStart") Instant periodStart);
}
