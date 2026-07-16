package com.aiworkspace.documents.domain.event;

import com.aiworkspace.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record DocumentUploadedEvent(
        UUID documentId,
        UUID ownerId,
        String title,
        String mimeType,
        String storagePath,
        Instant occurredAt
) implements DomainEvent {}
