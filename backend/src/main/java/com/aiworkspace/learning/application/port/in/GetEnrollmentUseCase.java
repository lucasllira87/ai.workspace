package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.dto.EnrollmentDto;

import java.util.UUID;

public interface GetEnrollmentUseCase {
    EnrollmentDto getById(UUID enrollmentId, UUID userId);
}
