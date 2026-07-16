package com.aiworkspace.billing.infrastructure.notification;

import com.aiworkspace.billing.application.port.out.BillingNotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Notification adapter using structured logs.
 * Replace with email/Slack/push adapter when notification module is available.
 */
@Component
public class LogBillingNotificationAdapter implements BillingNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LogBillingNotificationAdapter.class);

    @Override
    public void notifyPaymentFailed(UUID userId, UUID invoiceId) {
        log.warn("BILLING_NOTIFICATION payment_failed userId={} invoiceId={}", userId, invoiceId);
    }

    @Override
    public void notifyTrialExpiringSoon(UUID userId, int daysRemaining) {
        log.info("BILLING_NOTIFICATION trial_expiring_soon userId={} daysRemaining={}", userId, daysRemaining);
    }

    @Override
    public void notifySubscriptionCanceled(UUID userId) {
        log.info("BILLING_NOTIFICATION subscription_canceled userId={}", userId);
    }
}
