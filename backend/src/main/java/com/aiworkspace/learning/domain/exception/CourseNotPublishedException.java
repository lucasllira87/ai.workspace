package com.aiworkspace.learning.domain.exception;

import com.aiworkspace.shared.exception.DomainException;

import java.util.UUID;

public class CourseNotPublishedException extends DomainException {
    public CourseNotPublishedException(UUID courseId) {
        super("Course is not published and cannot be enrolled in: " + courseId);
    }
}
