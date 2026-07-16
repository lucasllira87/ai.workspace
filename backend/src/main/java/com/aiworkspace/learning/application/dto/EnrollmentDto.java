package com.aiworkspace.learning.application.dto;

import com.aiworkspace.learning.domain.model.Enrollment;
import com.aiworkspace.learning.domain.model.EnrollmentStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EnrollmentDto(UUID id, UUID userId, UUID courseId, EnrollmentStatus status,
                             List<LessonProgressDto> lessonProgress,
                             Instant enrolledAt, Instant completedAt) {
    public static EnrollmentDto from(Enrollment enrollment) {
        List<LessonProgressDto> progress = enrollment.getLessonProgressMap().values()
                .stream()
                .map(LessonProgressDto::from)
                .toList();
        return new EnrollmentDto(
                enrollment.getId(), enrollment.getUserId(), enrollment.getCourseId(),
                enrollment.getStatus(), progress,
                enrollment.getEnrolledAt(), enrollment.getCompletedAt());
    }
}
