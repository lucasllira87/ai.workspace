package com.aiworkspace.learning.domain.exception;

import com.aiworkspace.shared.exception.DomainException;

import java.util.UUID;

public class LessonNotStartedException extends DomainException {
    public LessonNotStartedException(UUID lessonId) {
        super("Lesson must be started before it can be completed: " + lessonId);
    }
}
