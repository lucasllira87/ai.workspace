package com.aiworkspace.billing.application.service;

import com.aiworkspace.billing.application.command.RecordUsageCommand;
import com.aiworkspace.billing.application.dto.QuotaStatusDto;
import com.aiworkspace.billing.application.dto.UsageSummaryDto;
import com.aiworkspace.billing.application.port.in.CheckQuotaUseCase;
import com.aiworkspace.billing.application.port.in.GetUsageSummaryUseCase;
import com.aiworkspace.billing.application.port.in.RecordUsageUseCase;
import com.aiworkspace.billing.application.port.out.PlanRepository;
import com.aiworkspace.billing.application.port.out.SubscriptionRepository;
import com.aiworkspace.billing.application.port.out.UsageLedgerRepository;
import com.aiworkspace.billing.domain.exception.PlanNotFoundException;
import com.aiworkspace.billing.domain.exception.QuotaExceededException;
import com.aiworkspace.billing.domain.exception.SubscriptionNotFoundException;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.Plan;
import com.aiworkspace.billing.domain.model.PlanQuota;
import com.aiworkspace.billing.domain.model.Subscription;
import com.aiworkspace.billing.domain.model.UsageEntry;
import com.aiworkspace.billing.domain.model.UsageLedger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UsageService implements RecordUsageUseCase, CheckQuotaUseCase, GetUsageSummaryUseCase {

    private final UsageLedgerRepository usageLedgerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Counter usageRecordedCounter;
    private final Counter quotaViolationCounter;

    public UsageService(UsageLedgerRepository usageLedgerRepository,
                         SubscriptionRepository subscriptionRepository,
                         PlanRepository planRepository,
                         ApplicationEventPublisher eventPublisher,
                         MeterRegistry meterRegistry) {
        this.usageLedgerRepository = usageLedgerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.eventPublisher = eventPublisher;
        this.usageRecordedCounter = Counter.builder("billing.usage.recorded")
                .description("Total usage entries recorded").register(meterRegistry);
        this.quotaViolationCounter = Counter.builder("billing.quota.violations")
                .description("Total quota enforcement violations").register(meterRegistry);
    }

    @Override
    @Transactional
    public void record(RecordUsageCommand command) {
        Subscription subscription = subscriptionRepository.findActiveByUserId(command.userId())
                .orElseThrow(() -> new SubscriptionNotFoundException(command.userId()));

        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("id:" + subscription.getPlanId()));

        BillingPeriod period = subscription.getCurrentPeriod();
        UsageLedger ledger = usageLedgerRepository.findOrCreate(command.userId(), period);

        UsageEntry entry = UsageEntry.of(command.module(), command.operation(),
                command.tokenCount(), command.documentCount(), command.storageBytes());

        ledger.record(entry, plan.getQuota());
        usageLedgerRepository.save(ledger);
        ledger.getDomainEvents().forEach(eventPublisher::publishEvent);
        usageRecordedCounter.increment();
    }

    @Override
    @Transactional(readOnly = true)
    public QuotaStatusDto check(UUID userId) {
        Subscription subscription = subscriptionRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new SubscriptionNotFoundException(userId));

        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("id:" + subscription.getPlanId()));

        PlanQuota quota = plan.getQuota();
        UsageLedger ledger = usageLedgerRepository.findOrCreate(userId, subscription.getCurrentPeriod());

        return new QuotaStatusDto(
                userId,
                !ledger.isOverTokenLimit(quota),
                !ledger.isOverDocumentLimit(quota),
                !ledger.isOverStorageLimit(quota),
                ledger.getTotals().totalTokens(),
                quota.maxTokensPerMonth(),
                ledger.getTotals().totalDocuments(),
                quota.maxDocuments()
        );
    }

    @Override
    @Transactional
    public void enforceTokenQuota(UUID userId) {
        QuotaStatusDto status = check(userId);
        if (!status.tokensAvailable()) {
            quotaViolationCounter.increment();
            throw new QuotaExceededException("tokens");
        }
    }

    @Override
    @Transactional
    public void enforceDocumentQuota(UUID userId) {
        QuotaStatusDto status = check(userId);
        if (!status.documentsAvailable()) {
            quotaViolationCounter.increment();
            throw new QuotaExceededException("documents");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UsageSummaryDto getCurrentPeriod(UUID userId) {
        Subscription subscription = subscriptionRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new SubscriptionNotFoundException(userId));

        UsageLedger ledger = usageLedgerRepository.findOrCreate(userId, subscription.getCurrentPeriod());
        return UsageSummaryDto.from(ledger);
    }
}
