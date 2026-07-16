package com.aiworkspace.billing.infrastructure.persistence.repository;

import com.aiworkspace.billing.infrastructure.persistence.entity.PlanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanJpaRepository extends JpaRepository<PlanJpaEntity, UUID> {

    Optional<PlanJpaEntity> findByName(String name);

    @Query("SELECT p FROM PlanJpaEntity p WHERE p.name = 'FREE' AND p.active = true")
    Optional<PlanJpaEntity> findFreePlan();

    List<PlanJpaEntity> findAllByActiveTrueOrderByPriceAmountAsc();
}
