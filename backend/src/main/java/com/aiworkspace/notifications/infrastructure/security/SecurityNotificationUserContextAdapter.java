package com.aiworkspace.notifications.infrastructure.security;

import com.aiworkspace.notifications.application.port.out.NotificationUserContextPort;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityNotificationUserContextAdapter implements NotificationUserContextPort {

    @Override
    public UUID currentUserId() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID in security context: " + name);
        }
    }

    @Override
    public String emailForUser(UUID userId) {
        // Email lookup delegated to IAM module via shared UserProfile query
        // In a full implementation this would call an IamPort or shared user service
        // For now we return a placeholder — the email adapter logs the call
        throw new UnsupportedOperationException(
                "emailForUser must be implemented via IamPort once IAM exposes a query endpoint");
    }
}
