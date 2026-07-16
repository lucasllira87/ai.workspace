package com.aiworkspace.documents.domain.exception;

import com.aiworkspace.documents.domain.model.DocumentStatus;
import com.aiworkspace.shared.exception.DomainException;

import java.util.UUID;

public class DocumentNotIndexedException extends DomainException {
    public DocumentNotIndexedException(UUID documentId, DocumentStatus status) {
        super("Document " + documentId + " is not indexed (current status: " + status + ")");
    }
}
