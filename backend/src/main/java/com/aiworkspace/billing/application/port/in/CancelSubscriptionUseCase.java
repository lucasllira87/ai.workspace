package com.aiworkspace.billing.application.port.in;

import java.util.UUID;

public interface CancelSubscriptionUseCase {
    void cancel(UUID userId);
}
