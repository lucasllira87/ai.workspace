package com.aiworkspace.audit.infrastructure.listener;

import com.aiworkspace.audit.application.command.RecordAuditEventCommand;
import com.aiworkspace.audit.application.port.in.RecordAuditEventUseCase;
import com.aiworkspace.audit.domain.model.AuditEventType;
import com.aiworkspace.billing.domain.event.PaymentFailedEvent;
import com.aiworkspace.billing.domain.event.PaymentSucceededEvent;
import com.aiworkspace.billing.domain.event.SubscriptionActivatedEvent;
import com.aiworkspace.billing.domain.event.SubscriptionCanceledEvent;
import com.aiworkspace.billing.domain.event.SubscriptionExpiredEvent;
import com.aiworkspace.billing.domain.event.SubscriptionRenewedEvent;
import com.aiworkspace.billing.domain.event.TrialStartedEvent;
import com.aiworkspace.billing.domain.event.UsageLimitExceededEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
public class BillingAuditListener {

    private static final Logger log = LoggerFactory.getLogger(BillingAuditListener.class);

    private final RecordAuditEventUseCase recordAuditEvent;

    public BillingAuditListener(RecordAuditEventUseCase recordAuditEvent) {
        this.recordAuditEvent = recordAuditEvent;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTrialStarted(TrialStartedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.TRIAL_STARTED, "billing",
                "Trial subscription started",
                Map.of("subscriptionId", event.subscriptionId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionActivated(SubscriptionActivatedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.SUBSCRIPTION_ACTIVATED, "billing",
                "Subscription activated",
                Map.of("subscriptionId", event.subscriptionId().toString(),
                       "planId", event.planId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionRenewed(SubscriptionRenewedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.SUBSCRIPTION_RENEWED, "billing",
                "Subscription renewed",
                Map.of("subscriptionId", event.subscriptionId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionCanceled(SubscriptionCanceledEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.SUBSCRIPTION_CANCELED, "billing",
                "Subscription canceled",
                Map.of("subscriptionId", event.subscriptionId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionExpired(SubscriptionExpiredEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.SUBSCRIPTION_EXPIRED, "billing",
                "Subscription expired",
                Map.of("subscriptionId", event.subscriptionId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.PAYMENT_SUCCEEDED, "billing",
                "Payment succeeded: " + event.amount().amount() + " " + event.amount().currency(),
                Map.of("invoiceId", event.invoiceId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.PAYMENT_FAILED, "billing",
                "Payment failed",
                Map.of("invoiceId", event.invoiceId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuotaExceeded(UsageLimitExceededEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId(), AuditEventType.QUOTA_EXCEEDED, "billing",
                "Usage quota exceeded: " + event.limitType(),
                Map.of("limitType", event.limitType(),
                       "current", String.valueOf(event.currentValue()),
                       "max", String.valueOf(event.maxValue()))
        ));
    }

    private void recordSafely(RecordAuditEventCommand command) {
        try {
            recordAuditEvent.record(command);
        } catch (Exception e) {
            log.error("Failed to record audit event: type={} reason={}",
                    command.eventType(), e.getMessage());
        }
    }
}
