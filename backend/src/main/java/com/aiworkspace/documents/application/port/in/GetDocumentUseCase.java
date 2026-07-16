package com.aiworkspace.documents.application.port.in;

import com.aiworkspace.documents.application.dto.DocumentDto;

import java.util.UUID;

public interface GetDocumentUseCase {
    DocumentDto getById(UUID documentId, UUID ownerId);
}
