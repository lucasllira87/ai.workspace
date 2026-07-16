package com.aiworkspace.learning.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

import java.util.UUID;

public class CourseNotFoundException extends NotFoundException {
    public CourseNotFoundException(UUID courseId) {
        super("Course not found: " + courseId);
    }
}
