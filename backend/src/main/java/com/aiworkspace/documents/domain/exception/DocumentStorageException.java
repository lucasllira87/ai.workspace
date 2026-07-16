package com.aiworkspace.documents.domain.exception;

// Extends RuntimeException directly — storage failures are infrastructure errors (→ HTTP 500)
public class DocumentStorageException extends RuntimeException {
    public DocumentStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
