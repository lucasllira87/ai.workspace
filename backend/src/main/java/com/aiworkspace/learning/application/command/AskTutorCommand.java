package com.aiworkspace.learning.application.command;

import java.util.UUID;

public record AskTutorCommand(UUID enrollmentId, UUID lessonId, UUID userId, String question) {}
