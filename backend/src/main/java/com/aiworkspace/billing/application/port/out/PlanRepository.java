package com.aiworkspace.billing.application.port.out;

import com.aiworkspace.billing.domain.model.Plan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository {
    Optional<Plan> findById(UUID id);

    Optional<Plan> findByName(String name);

    Optional<Plan> findFreePlan();

    List<Plan> findAllActive();
}
