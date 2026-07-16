package com.aiworkspace.billing.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

import java.util.UUID;

public class SubscriptionNotFoundException extends NotFoundException {
    public SubscriptionNotFoundException(UUID userId) {
        super("No active subscription found for user: " + userId);
    }
}
