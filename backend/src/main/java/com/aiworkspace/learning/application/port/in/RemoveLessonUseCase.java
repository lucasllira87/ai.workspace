package com.aiworkspace.learning.application.port.in;

import java.util.UUID;

public interface RemoveLessonUseCase {
    void removeLesson(UUID courseId, UUID lessonId, UUID ownerId);
}
