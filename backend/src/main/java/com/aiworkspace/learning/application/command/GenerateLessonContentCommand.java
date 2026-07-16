package com.aiworkspace.learning.application.command;

import java.util.UUID;

public record GenerateLessonContentCommand(UUID courseId, UUID lessonId, UUID ownerId, String topic) {}
