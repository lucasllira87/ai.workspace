package com.aiworkspace.aicore.domain.exception;

import com.aiworkspace.shared.exception.DomainException;

public class IngestionException extends DomainException {

    public IngestionException(String message) {
        super(message);
    }

    public IngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
