package com.aiworkspace.learning.application.command;

import java.util.UUID;

public record UpdateLessonCommand(UUID courseId, UUID lessonId, UUID ownerId,
                                   String title, String content, String videoUrl,
                                   int estimatedMinutes) {}
