package com.aiworkspace.billing.application.dto;

import java.util.UUID;

public record QuotaStatusDto(UUID userId, boolean tokensAvailable, boolean documentsAvailable,
                              boolean storageAvailable, long tokensUsed, long tokensLimit,
                              int documentsUsed, int documentsLimit) {}
