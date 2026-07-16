package com.aiworkspace.documents.domain.exception;

import com.aiworkspace.shared.exception.DomainException;

public class UnsupportedDocumentFormatException extends DomainException {
    public UnsupportedDocumentFormatException(String mimeType) {
        super("Unsupported document format: " + mimeType);
    }
}
