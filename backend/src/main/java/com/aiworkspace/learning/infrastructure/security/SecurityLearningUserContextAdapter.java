package com.aiworkspace.learning.infrastructure.security;

import com.aiworkspace.learning.application.port.out.LearningUserContextPort;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityLearningUserContextAdapter implements LearningUserContextPort {

    @Override
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new DomainException("No authenticated user in context");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID in security context: " + authentication.getName());
        }
    }
}
