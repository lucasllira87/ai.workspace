package com.aiworkspace.learning.application.command;

import java.util.UUID;

public record GenerateQuizCommand(UUID courseId, UUID lessonId, UUID ownerId, int questionCount) {}
