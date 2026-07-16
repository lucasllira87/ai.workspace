package com.aiworkspace.notifications.domain.exception;

import com.aiworkspace.shared.exception.DomainException;

import java.util.UUID;

public class NotificationNotFoundException extends DomainException {

    public NotificationNotFoundException(UUID id) {
        super("Notification not found: " + id);
    }
}
