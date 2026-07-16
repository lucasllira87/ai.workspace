package com.aiworkspace.audit.infrastructure.listener;

import com.aiworkspace.audit.application.command.RecordAuditEventCommand;
import com.aiworkspace.audit.application.port.in.RecordAuditEventUseCase;
import com.aiworkspace.audit.domain.model.AuditEventType;
import com.aiworkspace.iam.domain.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
public class IamAuditListener {

    private static final Logger log = LoggerFactory.getLogger(IamAuditListener.class);

    private final RecordAuditEventUseCase recordAuditEvent;

    public IamAuditListener(RecordAuditEventUseCase recordAuditEvent) {
        this.recordAuditEvent = recordAuditEvent;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        recordSafely(RecordAuditEventCommand.of(
                event.userId().value(),
                AuditEventType.USER_REGISTERED,
                "iam",
                "User registered with email " + event.email().value(),
                Map.of("email", event.email().value())
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
