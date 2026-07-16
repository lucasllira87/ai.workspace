package com.aiworkspace.notifications.infrastructure.listener;

import com.aiworkspace.billing.domain.event.PaymentFailedEvent;
import com.aiworkspace.billing.domain.event.SubscriptionCanceledEvent;
import com.aiworkspace.billing.domain.event.TrialStartedEvent;
import com.aiworkspace.billing.domain.event.UsageLimitExceededEvent;
import com.aiworkspace.notifications.application.command.SendNotificationCommand;
import com.aiworkspace.notifications.application.port.in.SendNotificationUseCase;
import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
public class BillingEventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(BillingEventNotificationListener.class);

    private final SendNotificationUseCase sendNotification;

    public BillingEventNotificationListener(SendNotificationUseCase sendNotification) {
        this.sendNotification = sendNotification;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTrialStarted(TrialStartedEvent event) {
        sendSafely(new SendNotificationCommand(event.userId(), NotificationType.TRIAL_STARTED,
                NotificationChannel.EMAIL, Map.of()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentFailed(PaymentFailedEvent event) {
        sendSafely(new SendNotificationCommand(event.userId(), NotificationType.PAYMENT_FAILED,
                NotificationChannel.EMAIL, Map.of("invoiceId", event.invoiceId().toString())));
        sendSafely(new SendNotificationCommand(event.userId(), NotificationType.PAYMENT_FAILED,
                NotificationChannel.IN_APP, Map.of("invoiceId", event.invoiceId().toString())));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionCanceled(SubscriptionCanceledEvent event) {
        sendSafely(new SendNotificationCommand(event.userId(), NotificationType.SUBSCRIPTION_CANCELED,
                NotificationChannel.EMAIL, Map.of()));
        sendSafely(new SendNotificationCommand(event.userId(), NotificationType.SUBSCRIPTION_CANCELED,
                NotificationChannel.IN_APP, Map.of()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUsageLimitExceeded(UsageLimitExceededEvent event) {
        sendSafely(new SendNotificationCommand(event.userId(), NotificationType.QUOTA_EXCEEDED,
                NotificationChannel.IN_APP, Map.of("limitType", event.limitType())));
    }

    private void sendSafely(SendNotificationCommand command) {
        try {
            sendNotification.send(command);
        } catch (Exception e) {
            log.error("Failed to dispatch notification: type={} userId={} reason={}",
                    command.type(), command.userId(), e.getMessage());
        }
    }
}
