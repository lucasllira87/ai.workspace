package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.dto.EnrollmentDto;

import java.util.UUID;

public interface CompleteLessonUseCase {
    EnrollmentDto completeLesson(UUID enrollmentId, UUID lessonId, UUID userId);
}
