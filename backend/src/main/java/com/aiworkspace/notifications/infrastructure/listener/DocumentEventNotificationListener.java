package com.aiworkspace.notifications.infrastructure.listener;

import com.aiworkspace.documents.domain.event.DocumentIndexedEvent;
import com.aiworkspace.documents.domain.event.DocumentIndexingFailedEvent;
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
public class DocumentEventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentEventNotificationListener.class);

    private final SendNotificationUseCase sendNotification;

    public DocumentEventNotificationListener(SendNotificationUseCase sendNotification) {
        this.sendNotification = sendNotification;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentIndexed(DocumentIndexedEvent event) {
        try {
            sendNotification.send(new SendNotificationCommand(event.ownerId(),
                    NotificationType.DOCUMENT_INDEXED, NotificationChannel.IN_APP,
                    Map.of("documentTitle", event.title())));
        } catch (Exception e) {
            log.error("Failed to dispatch document-indexed notification: ownerId={} reason={}",
                    event.ownerId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentIndexingFailed(DocumentIndexingFailedEvent event) {
        try {
            sendNotification.send(new SendNotificationCommand(event.ownerId(),
                    NotificationType.DOCUMENT_FAILED, NotificationChannel.IN_APP,
                    Map.of("documentTitle", event.title(), "reason", event.reason())));
        } catch (Exception e) {
            log.error("Failed to dispatch document-failed notification: ownerId={} reason={}",
                    event.ownerId(), e.getMessage());
        }
    }
}
