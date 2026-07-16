package com.aiworkspace.aicore.domain.exception;

import com.aiworkspace.shared.exception.ServiceUnavailableException;

public class AllProvidersExhaustedException extends ServiceUnavailableException {

    public AllProvidersExhaustedException(String message) {
        super(message);
    }

    public AllProvidersExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }
}
