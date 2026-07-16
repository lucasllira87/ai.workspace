package com.aiworkspace.billing.application.port.in;

import com.aiworkspace.billing.application.dto.SubscriptionDto;

import java.util.UUID;

public interface GetSubscriptionUseCase {
    SubscriptionDto getByUserId(UUID userId);
}
