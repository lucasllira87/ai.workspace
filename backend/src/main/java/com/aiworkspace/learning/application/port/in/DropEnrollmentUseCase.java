package com.aiworkspace.learning.application.port.in;

import java.util.UUID;

public interface DropEnrollmentUseCase {
    void drop(UUID enrollmentId, UUID userId);
}
