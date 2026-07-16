package com.aiworkspace.learning.application.dto;

import com.aiworkspace.learning.domain.model.LessonProgress;
import com.aiworkspace.learning.domain.model.LessonProgressStatus;

import java.time.Instant;
import java.util.UUID;

public record LessonProgressDto(UUID lessonId, LessonProgressStatus status,
                                  Instant startedAt, Instant completedAt) {
    public static LessonProgressDto from(LessonProgress p) {
        return new LessonProgressDto(p.lessonId(), p.status(), p.startedAt(), p.completedAt());
    }
}
