package com.aiworkspace.documents.infrastructure.security;

import com.aiworkspace.documents.application.port.out.UserContextPort;
import com.aiworkspace.shared.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUserContextAdapter implements UserContextPort {

    @Override
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("No authenticated user in context");
        }
        return auth.getName();
    }
}
