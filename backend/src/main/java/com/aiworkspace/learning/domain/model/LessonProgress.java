package com.aiworkspace.learning.domain.model;

import java.time.Instant;
import java.util.UUID;

public record LessonProgress(
        UUID lessonId,
        LessonProgressStatus status,
        Instant startedAt,
        Instant completedAt
) {
    public static LessonProgress notStarted(UUID lessonId) {
        return new LessonProgress(lessonId, LessonProgressStatus.NOT_STARTED, null, null);
    }

    public LessonProgress start() {
        return new LessonProgress(lessonId, LessonProgressStatus.IN_PROGRESS, Instant.now(), null);
    }

    public LessonProgress complete() {
        Instant start = startedAt != null ? startedAt : Instant.now();
        return new LessonProgress(lessonId, LessonProgressStatus.COMPLETED, start, Instant.now());
    }
}
