package com.aiworkspace.learning.domain.exception;

import com.aiworkspace.shared.exception.NotFoundException;

import java.util.UUID;

public class LessonNotFoundException extends NotFoundException {
    public LessonNotFoundException(UUID lessonId) {
        super("Lesson not found: " + lessonId);
    }
}
