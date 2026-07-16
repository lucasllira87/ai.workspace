package com.aiworkspace.billing.infrastructure.persistence.adapter;

import com.aiworkspace.billing.application.port.out.UsageLedgerRepository;
import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.UsageEntry;
import com.aiworkspace.billing.domain.model.UsageLedger;
import com.aiworkspace.billing.domain.model.UsageSummary;
import com.aiworkspace.billing.infrastructure.persistence.entity.UsageEntryJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.entity.UsageLedgerJpaEntity;
import com.aiworkspace.billing.infrastructure.persistence.repository.UsageLedgerJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UsageLedgerRepositoryAdapter implements UsageLedgerRepository {

    private final UsageLedgerJpaRepository jpaRepository;

    public UsageLedgerRepositoryAdapter(UsageLedgerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UsageLedger save(UsageLedger ledger) {
        UsageLedgerJpaEntity entity = toEntity(ledger);
        jpaRepository.save(entity);
        return ledger;
    }

    @Override
    public Optional<UsageLedger> findByUserIdAndPeriod(UUID userId, BillingPeriod period) {
        return jpaRepository.findByUserIdAndPeriodStart(userId, period.startAt())
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public UsageLedger findOrCreate(UUID userId, BillingPeriod period) {
        return jpaRepository.findByUserIdAndPeriodStart(userId, period.startAt())
                .map(this::toDomain)
                .orElseGet(() -> {
                    UsageLedger newLedger = UsageLedger.create(UUID.randomUUID(), userId, period);
                    jpaRepository.save(toEntity(newLedger));
                    return newLedger;
                });
    }

    private UsageLedgerJpaEntity toEntity(UsageLedger ledger) {
        UsageLedgerJpaEntity entity = new UsageLedgerJpaEntity();
        entity.setId(ledger.getId());
        entity.setUserId(ledger.getUserId());
        entity.setPeriodStart(ledger.getPeriod().startAt());
        entity.setPeriodEnd(ledger.getPeriod().endAt());
        entity.setTotalTokens(ledger.getTotals().totalTokens());
        entity.setTotalDocuments(ledger.getTotals().totalDocuments());
        entity.setTotalStorageBytes(ledger.getTotals().totalStorageBytes());
        entity.setCreatedAt(ledger.getCreatedAt());
        entity.setUpdatedAt(ledger.getUpdatedAt());

        List<UsageEntryJpaEntity> entryEntities = new ArrayList<>();
        for (UsageEntry entry : ledger.getEntries()) {
            UsageEntryJpaEntity ee = new UsageEntryJpaEntity();
            ee.setId(entry.id());
            ee.setLedger(entity);
            ee.setModule(entry.module());
            ee.setOperation(entry.operation());
            ee.setTokenCount(entry.tokenCount());
            ee.setDocumentCount(entry.documentCount());
            ee.setStorageBytes(entry.storageBytes());
            ee.setRecordedAt(entry.recordedAt());
            entryEntities.add(ee);
        }
        entity.setEntries(entryEntities);
        return entity;
    }

    private UsageLedger toDomain(UsageLedgerJpaEntity entity) {
        // Entries are LAZY — only loaded when accessed. For quota checks we only need totals.
        UsageSummary totals = new UsageSummary(
                entity.getTotalTokens(),
                entity.getTotalDocuments(),
                entity.getTotalStorageBytes()
        );
        BillingPeriod period = new BillingPeriod(entity.getPeriodStart(), entity.getPeriodEnd());
        return UsageLedger.reconstitute(entity.getId(), entity.getUserId(), period,
                List.of(), totals, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
