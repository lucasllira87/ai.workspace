package com.aiworkspace.learning.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LessonCompletedEvent(UUID enrollmentId, UUID userId, UUID courseId,
                                    UUID lessonId, Instant occurredAt) {}
