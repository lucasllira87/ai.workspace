package com.aiworkspace.learning.application.port.in;

import com.aiworkspace.learning.application.dto.EnrollmentDto;

import java.util.UUID;

public interface StartLessonUseCase {
    EnrollmentDto startLesson(UUID enrollmentId, UUID lessonId, UUID userId);
}
