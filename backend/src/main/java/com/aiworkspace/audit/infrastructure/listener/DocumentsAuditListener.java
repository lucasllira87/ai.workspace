package com.aiworkspace.audit.infrastructure.listener;

import com.aiworkspace.audit.application.command.RecordAuditEventCommand;
import com.aiworkspace.audit.application.port.in.RecordAuditEventUseCase;
import com.aiworkspace.audit.domain.model.AuditEventType;
import com.aiworkspace.documents.domain.event.DocumentDeletedEvent;
import com.aiworkspace.documents.domain.event.DocumentIndexedEvent;
import com.aiworkspace.documents.domain.event.DocumentUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
public class DocumentsAuditListener {

    private static final Logger log = LoggerFactory.getLogger(DocumentsAuditListener.class);

    private final RecordAuditEventUseCase recordAuditEvent;

    public DocumentsAuditListener(RecordAuditEventUseCase recordAuditEvent) {
        this.recordAuditEvent = recordAuditEvent;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentUploaded(DocumentUploadedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.ownerId(), AuditEventType.DOCUMENT_UPLOADED, "documents",
                "Uploaded document: " + event.title(),
                Map.of("documentId", event.documentId().toString(),
                       "mimeType", event.mimeType())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentIndexed(DocumentIndexedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.ownerId(), AuditEventType.DOCUMENT_INDEXED, "documents",
                "Indexed document: " + event.title(),
                Map.of("documentId", event.documentId().toString())
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentDeleted(DocumentDeletedEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.ownerId(), AuditEventType.DOCUMENT_DELETED, "documents",
                "Document deleted",
                Map.of("documentId", event.documentId().toString())
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
