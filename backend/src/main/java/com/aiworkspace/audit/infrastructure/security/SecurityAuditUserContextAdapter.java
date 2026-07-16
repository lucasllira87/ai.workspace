package com.aiworkspace.audit.infrastructure.security;

import com.aiworkspace.audit.application.port.out.AuditUserContextPort;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityAuditUserContextAdapter implements AuditUserContextPort {

    @Override
    public UUID currentUserId() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Invalid user ID in security context: " + name);
        }
    }
}
