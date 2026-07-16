package com.aiworkspace.learning.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

import java.util.UUID;

public class EnrollmentNotFoundException extends NotFoundException {
    public EnrollmentNotFoundException(UUID enrollmentId) {
        super("Enrollment not found: " + enrollmentId);
    }
}
