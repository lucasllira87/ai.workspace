package com.aiworkspace.audit.infrastructure.stats;

import com.aiworkspace.audit.application.port.out.BillingStatsPort;
import com.aiworkspace.audit.domain.model.BillingStats;
import com.aiworkspace.billing.infrastructure.persistence.entity.PlanJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.entity.SubscriptionJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.entity.UsageLedgerJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.repository.PlanJpaRepository;
import com.aiworkspace.billing.infrastructure.persistence.repository.SubscriptionJpaRepository;
import com.aiworkspace.billing.infrastructure.persistence.repository.UsageLedgerJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Component
public class BillingStatsAdapter implements BillingStatsPort {

    private final SubscriptionJpaRepository subscriptionJpaRepository;
    private final PlanJpaRepository planJpaRepository;
    private final UsageLedgerJpaRepository usageLedgerJpaRepository;

    public BillingStatsAdapter(SubscriptionJpaRepository subscriptionJpaRepository,
                                PlanJpaRepository planJpaRepository,
                                UsageLedgerJpaRepository usageLedgerJpaRepository) {
        this.subscriptionJpaRepository = subscriptionJpaRepository;
        this.planJpaRepository = planJpaRepository;
        this.usageLedgerJpaRepository = usageLedgerJpaRepository;
    }

    @Override
    public BillingStats getStatsForUser(UUID userId) {
        Optional<SubscriptionJpaEntity> subOpt = subscriptionJpaRepository.findActiveByUserId(userId);
        if (subOpt.isEmpty()) {
            return BillingStats.unavailable();
        }

        SubscriptionJpaEntity sub = subOpt.get();
        Optional<PlanJpaEntity> planOpt = planJpaRepository.findById(sub.getPlanId());
        String planName = planOpt.map(PlanJpaEntity::getName).orElse("UNKNOWN");
        long tokenQuota = planOpt.map(PlanJpaEntity::getMaxTokensPerMonth).orElse(0L);

        Instant periodStart = currentMonthStart();
        long tokensUsed = usageLedgerJpaRepository
                .findByUserIdAndPeriodStart(userId, periodStart)
                .map(UsageLedgerJpaEntity::getTotalTokens)
                .orElse(0L);

        long daysUntilRenewal = ChronoUnit.DAYS.between(Instant.now(), sub.getPeriodEnd());

        return new BillingStats(planName, sub.getStatus(), tokensUsed,
                tokenQuota, Math.max(0, daysUntilRenewal));
    }

    private Instant currentMonthStart() {
        return Instant.now()
                .atZone(ZoneOffset.UTC)
                .withDayOfMonth(1)
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant();
    }
}
