package com.aiworkspace.documents.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

import java.util.UUID;

public class DocumentNotFoundException extends NotFoundException {
    public DocumentNotFoundException(UUID documentId) {
        super("Document not found: " + documentId);
    }
}
