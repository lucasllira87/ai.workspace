package com.aiworkspace.learning.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CourseCreatedEvent(UUID courseId, UUID ownerId, String title, Instant occurredAt) {}
