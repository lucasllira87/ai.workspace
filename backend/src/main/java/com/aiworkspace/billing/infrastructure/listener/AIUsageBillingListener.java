package com.aiworkspace.billing.infrastructure.listener;

import com.aiworkspace.aicore.domain.event.AIRequestCompletedEvent;
import com.aiworkspace.billing.application.command.RecordUsageCommand;
import com.aiworkspace.billing.application.port.in.RecordUsageUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class AIUsageBillingListener {

    private static final Logger log = LoggerFactory.getLogger(AIUsageBillingListener.class);

    private final RecordUsageUseCase recordUsage;

    public AIUsageBillingListener(RecordUsageUseCase recordUsage) {
        this.recordUsage = recordUsage;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAIRequestCompleted(AIRequestCompletedEvent event) {
        if (!event.success()) return;

        // requestedBy is the userId string — skip if it's a system call ("system" or empty)
        String requestedBy = event.requestedBy();
        if (requestedBy == null || requestedBy.isBlank() || "system".equals(requestedBy)) return;

        UUID userId;
        try {
            userId = UUID.fromString(requestedBy);
        } catch (IllegalArgumentException e) {
            log.debug("Skipping billing for non-UUID requestedBy: {}", requestedBy);
            return;
        }

        long tokenCount = event.usage() != null ? event.usage().total() : 0;

        try {
            recordUsage.record(new RecordUsageCommand(
                    userId, event.module(), event.operation(), tokenCount, 0, 0));
        } catch (Exception e) {
            // Billing failure must not affect the AI response already returned
            log.error("Failed to record AI usage for userId={} module={}",
                    userId, event.module(), e);
        }
    }
}
