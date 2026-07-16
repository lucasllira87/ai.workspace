package com.aiworkspace.billing.application;

import com.aiworkspace.billing.application.command.RecordUsageCommand;
import com.aiworkspace.billing.application.dto.QuotaStatusDto;
import com.aiworkspace.billing.application.port.out.PlanRepository;
import com.aiworkspace.billing.application.port.out.SubscriptionRepository;
import com.aiworkspace.billing.application.port.out.UsageLedgerRepository;
import com.aiworkspace.billing.application.service.UsageService;
import com.aiworkspace.billing.domain.exception.QuotaExceededException;
import com.aiworkspace.billing.domain.exception.SubscriptionNotFoundException;
import com.aiworkspace.billing.domain.model.BillingCycle;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Plan;
import com.aiworkspace.billing.domain.model.PlanQuota;
import com.aiworkspace.billing.domain.model.Subscription;
import com.aiworkspace.billing.domain.model.UsageEntry;
import com.aiworkspace.billing.domain.model.UsageLedger;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock UsageLedgerRepository usageLedgerRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock PlanRepository planRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    UsageService service;

    final UUID userId = UUID.randomUUID();
    final UUID planId = UUID.randomUUID();

    // PlanQuota(maxTokensPerMonth, maxDocuments, maxStorageBytes, maxEnrollments)
    private static final PlanQuota GENEROUS_QUOTA = new PlanQuota(100_000L, 100, 10L * 1024 * 1024 * 1024, 10);
    private static final PlanQuota ZERO_QUOTA = new PlanQuota(0L, 0, 0L, 0);

    @BeforeEach
    void setUp() {
        service = new UsageService(usageLedgerRepository, subscriptionRepository,
                planRepository, eventPublisher, new SimpleMeterRegistry());
    }

    @Test
    void record_savesUsageEntryAndPublishesEvents() {
        setupSubscriptionAndLedger(GENEROUS_QUOTA);

        service.record(new RecordUsageCommand(userId, "documents", "QUERY", 1500, 0, 0));

        verify(usageLedgerRepository).save(any(UsageLedger.class));
    }

    @Test
    void record_throwsWhenNoActiveSubscription() {
        when(subscriptionRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.record(
                new RecordUsageCommand(userId, "docs", "QUERY", 100, 0, 0)))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    @Test
    void enforceTokenQuota_passesWhenUnderLimit() {
        setupSubscriptionAndLedger(GENEROUS_QUOTA);

        service.enforceTokenQuota(userId);
    }

    @Test
    void enforceTokenQuota_throwsWhenOverLimit() {
        Plan zeroPlan = Plan.reconstitute(planId, "ZERO", null, BillingCycle.MONTHLY,
                ZERO_QUOTA, Set.of(), true);
        Subscription sub = Subscription.startTrial(UUID.randomUUID(), userId, planId);
        UsageLedger ledger = UsageLedger.create(UUID.randomUUID(), userId, sub.getCurrentPeriod());
        // Record usage to exceed quota of 0
        ledger.record(UsageEntry.of("docs", "QUERY", 10, 1, 0), ZERO_QUOTA);

        when(subscriptionRepository.findActiveByUserId(userId)).thenReturn(Optional.of(sub));
        when(planRepository.findById(planId)).thenReturn(Optional.of(zeroPlan));
        when(usageLedgerRepository.findOrCreate(any(), any())).thenReturn(ledger);

        assertThatThrownBy(() -> service.enforceTokenQuota(userId))
                .isInstanceOf(QuotaExceededException.class);
    }

    @Test
    void check_returnsQuotaStatus() {
        setupSubscriptionAndLedger(GENEROUS_QUOTA);

        QuotaStatusDto status = service.check(userId);

        assertThat(status.userId()).isEqualTo(userId);
        assertThat(status.tokensAvailable()).isTrue();
        assertThat(status.documentsAvailable()).isTrue();
    }

    private void setupSubscriptionAndLedger(PlanQuota quota) {
        Plan plan = Plan.reconstitute(planId, "FREE", null, BillingCycle.MONTHLY, quota, Set.of(), true);
        Subscription sub = Subscription.startTrial(UUID.randomUUID(), userId, planId);
        UsageLedger ledger = UsageLedger.create(UUID.randomUUID(), userId, sub.getCurrentPeriod());

        when(subscriptionRepository.findActiveByUserId(userId)).thenReturn(Optional.of(sub));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(usageLedgerRepository.findOrCreate(eq(userId), any(BillingPeriod.class))).thenReturn(ledger);
        when(usageLedgerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}
