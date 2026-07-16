package com.aiworkspace.aicore.domain.event;

import com.aiworkspace.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record DocumentIngestionCompletedEvent(
        UUID documentId,
        int chunkCount,
        int totalTokens,
        Instant occurredAt
) implements DomainEvent {}
