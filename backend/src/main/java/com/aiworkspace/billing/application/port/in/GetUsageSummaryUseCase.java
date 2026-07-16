package com.aiworkspace.billing.application.port.in;

import com.aiworkspace.billing.application.dto.UsageSummaryDto;

import java.util.UUID;

public interface GetUsageSummaryUseCase {
    UsageSummaryDto getCurrentPeriod(UUID userId);
}
