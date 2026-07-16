package com.aiworkspace.learning.presentation.response;

import com.aiworkspace.learning.application.dto.EnrollmentDto;
import com.aiworkspace.learning.application.dto.LessonProgressDto;
import com.aiworkspace.learning.domain.model.EnrollmentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID userId,
        UUID courseId,
        EnrollmentStatus status,
        List<LessonProgressDto> lessonProgress,
        Instant enrolledAt,
        Instant completedAt
) {
    public static EnrollmentResponse from(EnrollmentDto dto) {
        return new EnrollmentResponse(dto.id(), dto.userId(), dto.courseId(), dto.status(),
                dto.lessonProgress(), dto.enrolledAt(), dto.completedAt());
    }
}
