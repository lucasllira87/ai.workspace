package com.aiworkspace.learning.application.command;

import com.aiworkspace.learning.domain.model.LessonType;

import java.util.UUID;

public record AddLessonCommand(UUID courseId, UUID ownerId, String title, String content,
                                String videoUrl, LessonType type, int estimatedMinutes) {}
