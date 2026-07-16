package com.aiworkspace.documents.application.dto;

import com.aiworkspace.documents.domain.model.Document;
import com.aiworkspace.documents.domain.model.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentDto(
        UUID id,
        UUID ownerId,
        String title,
        String mimeType,
        long sizeBytes,
        DocumentStatus status,
        Integer chunkCount,
        String errorMessage,
        Instant indexedAt,
        Instant uploadedAt
) {
    public static DocumentDto from(Document doc) {
        return new DocumentDto(
                doc.getId(),
                doc.getOwnerId(),
                doc.getTitle(),
                doc.getMetadata().mimeType(),
                doc.getMetadata().sizeBytes(),
                doc.getStatus(),
                doc.getChunkCount(),
                doc.getErrorMessage(),
                doc.getIndexedAt(),
                doc.getUploadedAt());
    }
}
