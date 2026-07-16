package com.aiworkspace.iam.infrastructure.notification;

import com.aiworkspace.iam.application.port.out.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * No-op email adapter for MVP.
 * Replace with SendGridEmailAdapter when email integration is added (roadmap).
 */
@Component
public class NoOpEmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpEmailAdapter.class);

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        log.info("[EMAIL-NOOP] Password reset → to={} link={}", to, resetLink);
    }

    @Override
    public void sendWelcomeEmail(String to, String userName) {
        log.info("[EMAIL-NOOP] Welcome → to={} user={}", to, userName);
    }
}
