package com.aiworkspace.learning.domain.exception;

import com.aiworkspace.shared.exception.DomainException;

import java.util.UUID;

public class AlreadyEnrolledException extends DomainException {
    public AlreadyEnrolledException(UUID courseId) {
        super("User is already actively enrolled in course: " + courseId);
    }
}
