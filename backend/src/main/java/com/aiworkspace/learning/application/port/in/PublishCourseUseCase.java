package com.aiworkspace.learning.application.port.in;

import java.util.UUID;

public interface PublishCourseUseCase {
    void publish(UUID courseId, UUID ownerId);
}
