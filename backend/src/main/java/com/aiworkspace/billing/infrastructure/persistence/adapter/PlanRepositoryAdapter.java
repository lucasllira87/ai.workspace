package com.aiworkspace.billing.infrastructure.persistence.adapter;

import com.aiworkspace.billing.application.port.out.PlanRepository;
import com.aiworkspace.billing.domain.model.BillingCycle;
import com.aiworkspace.billing.domain.model.Money;
import com.aiworkspace.billing.domain.model.Plan;
import com.aiworkspace.billing.domain.model.PlanFeature;
import com.aiworkspace.billing.domain.model.PlanQuota;
import com.aiworkspace.billing.infrastructure.persistence.entity.PlanJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.repository.PlanJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PlanRepositoryAdapter implements PlanRepository {

    private final PlanJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public PlanRepositoryAdapter(PlanJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Plan> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Plan> findByName(String name) {
        return jpaRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public Optional<Plan> findFreePlan() {
        return jpaRepository.findFreePlan().map(this::toDomain);
    }

    @Override
    public List<Plan> findAllActive() {
        return jpaRepository.findAllByActiveTrueOrderByPriceAmountAsc().stream()
                .map(this::toDomain).toList();
    }

    private Plan toDomain(PlanJpaEntity entity) {
        Set<PlanFeature> features = parseFeatures(entity.getFeaturesJson());
        return Plan.reconstitute(
                entity.getId(),
                entity.getName(),
                Money.of(entity.getPriceAmount(), entity.getPriceCurrency()),
                BillingCycle.valueOf(entity.getBillingCycle()),
                new PlanQuota(entity.getMaxTokensPerMonth(), entity.getMaxDocuments(),
                        entity.getMaxStorageBytes(), entity.getMaxEnrollments()),
                features,
                entity.isActive()
        );
    }

    private Set<PlanFeature> parseFeatures(String featuresJson) {
        if (featuresJson == null || featuresJson.isBlank() || featuresJson.equals("{}")) {
            return new HashSet<>();
        }
        try {
            // PostgreSQL TEXT[] stored as JSON array string like '["AI_TUTOR","DOCUMENT_ASSISTANT"]'
            List<String> names = objectMapper.readValue(featuresJson, new TypeReference<>() {});
            return names.stream()
                    .filter(n -> !n.isBlank())
                    .map(PlanFeature::valueOf)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            // Fallback: comma-separated or PostgreSQL array literal {A,B}
            String cleaned = featuresJson.replaceAll("[{}\"\\[\\]\\s]", "");
            if (cleaned.isBlank()) return new HashSet<>();
            return Arrays.stream(cleaned.split(","))
                    .filter(s -> !s.isBlank())
                    .map(PlanFeature::valueOf)
                    .collect(Collectors.toSet());
        }
    }
}
