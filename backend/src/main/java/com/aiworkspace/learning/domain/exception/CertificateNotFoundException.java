package com.aiworkspace.learning.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

import java.util.UUID;

public class CertificateNotFoundException extends NotFoundException {
    public CertificateNotFoundException(UUID enrollmentId) {
        super("Certificate not found for enrollment: " + enrollmentId);
    }
}
