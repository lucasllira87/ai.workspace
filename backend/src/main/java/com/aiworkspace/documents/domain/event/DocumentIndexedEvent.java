package com.aiworkspace.documents.domain.event;

import com.aiworkspace.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record DocumentIndexedEvent(
        UUID documentId,
        UUID ownerId,
        String title,
        int chunkCount,
        Instant occurredAt
) implements DomainEvent {}
