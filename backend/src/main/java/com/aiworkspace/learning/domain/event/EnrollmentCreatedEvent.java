package com.aiworkspace.learning.domain.event;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentCreatedEvent(UUID enrollmentId, UUID userId, UUID courseId, Instant occurredAt) {}
