package com.aiworkspace.documents.presentation.response;

import com.aiworkspace.documents.application.dto.DocumentDto;
import com.aiworkspace.documents.domain.model.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String title,
        String mimeType,
        long sizeBytes,
        DocumentStatus status,
        Integer chunkCount,
        String errorMessage,
        Instant indexedAt,
        Instant uploadedAt
) {
    public static DocumentResponse from(DocumentDto dto) {
        return new DocumentResponse(
                dto.id(),
                dto.title(),
                dto.mimeType(),
                dto.sizeBytes(),
                dto.status(),
                dto.chunkCount(),
                dto.errorMessage(),
                dto.indexedAt(),
                dto.uploadedAt());
    }
}
