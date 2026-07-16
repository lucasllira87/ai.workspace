package com.aiworkspace.billing.application.port.out;

import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.UsageLedger;

import java.util.Optional;
import java.util.UUID;

public interface UsageLedgerRepository {
    UsageLedger save(UsageLedger ledger);

    Optional<UsageLedger> findByUserIdAndPeriod(UUID userId, BillingPeriod period);

    UsageLedger findOrCreate(UUID userId, BillingPeriod period);
}
