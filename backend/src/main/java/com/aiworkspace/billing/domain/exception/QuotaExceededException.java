package com.aiworkspace.billing.domain.exception;

import com.aiworkspace.shared.exception.DomainException;

public class QuotaExceededException extends DomainException {
    public QuotaExceededException(String limitType) {
        super("Usage quota exceeded for: " + limitType + ". Upgrade your plan to continue.");
    }
}
