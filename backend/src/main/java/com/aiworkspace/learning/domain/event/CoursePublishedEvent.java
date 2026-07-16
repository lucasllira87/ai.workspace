package com.aiworkspace.learning.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CoursePublishedEvent(UUID courseId, UUID ownerId, String title,
                                    int lessonCount, Instant occurredAt) {}
