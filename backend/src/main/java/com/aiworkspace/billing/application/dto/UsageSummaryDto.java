package com.aiworkspace.billing.application.dto;

import com.aiworkspace.billing.domain.model.BillingPeriod;
import com.aiworkspace.billing.domain.model.UsageLedger;
import com.aiworkspace.billing.domain.model.UsageSummary;

import java.util.UUID;

public record UsageSummaryDto(UUID userId, BillingPeriod period, UsageSummary totals) {
    public static UsageSummaryDto from(UsageLedger ledger) {
        return new UsageSummaryDto(ledger.getUserId(), ledger.getPeriod(), ledger.getTotals());
    }
}
