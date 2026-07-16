package com.aiworkspace.billing.application.port.out;

import java.util.UUID;

public interface BillingNotificationPort {
    void notifyPaymentFailed(UUID userId, UUID invoiceId);

    void notifyTrialExpiringSoon(UUID userId, int daysRemaining);

    void notifySubscriptionCanceled(UUID userId);
}
