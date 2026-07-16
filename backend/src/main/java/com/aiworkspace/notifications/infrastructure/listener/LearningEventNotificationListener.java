package com.aiworkspace.notifications.infrastructure.listener;

import com.aiworkspace.learning.domain.event.CertificateIssuedEvent;
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
public class LearningEventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(LearningEventNotificationListener.class);

    private final SendNotificationUseCase sendNotification;

    public LearningEventNotificationListener(SendNotificationUseCase sendNotification) {
        this.sendNotification = sendNotification;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCertificateIssued(CertificateIssuedEvent event) {
        try {
            sendNotification.send(new SendNotificationCommand(event.userId(),
                    NotificationType.CERTIFICATE_ISSUED, NotificationChannel.EMAIL,
                    Map.of("courseTitle", event.courseTitle(), "certificateId", event.certificateId().toString())));
            sendNotification.send(new SendNotificationCommand(event.userId(),
                    NotificationType.CERTIFICATE_ISSUED, NotificationChannel.IN_APP,
                    Map.of("courseTitle", event.courseTitle())));
        } catch (Exception e) {
            log.error("Failed to dispatch certificate notification: userId={} reason={}",
                    event.userId(), e.getMessage());
        }
    }
}
