package com.aiworkspace.audit.application.port.out;

import com.aiworkspace.audit.domain.model.BillingStats;

import java.util.UUID;

public interface BillingStatsPort {
    BillingStats getStatsForUser(UUID userId);
}
