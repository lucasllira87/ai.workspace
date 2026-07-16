package com.aiworkspace.documents.application.port.in;

import com.aiworkspace.documents.application.dto.DocumentDto;

import java.util.List;
import java.util.UUID;

public interface ListDocumentsUseCase {
    List<DocumentDto> listByOwner(UUID ownerId);
}
