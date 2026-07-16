package com.aiworkspace.billing.application.port.in;

import com.aiworkspace.billing.application.dto.QuotaStatusDto;

import java.util.UUID;

public interface CheckQuotaUseCase {
    QuotaStatusDto check(UUID userId);

    void enforceTokenQuota(UUID userId);

    void enforceDocumentQuota(UUID userId);
}
