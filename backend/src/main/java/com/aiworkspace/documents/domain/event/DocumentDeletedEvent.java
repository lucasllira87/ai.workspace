package com.aiworkspace.documents.domain.event;

import com.aiworkspace.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record DocumentDeletedEvent(
        UUID documentId,
        UUID ownerId,
        String storagePath,
        Instant occurredAt
) implements DomainEvent {}
