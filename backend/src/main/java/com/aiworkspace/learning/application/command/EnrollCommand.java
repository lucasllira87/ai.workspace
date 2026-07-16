package com.aiworkspace.learning.application.command;

import java.util.UUID;

public record EnrollCommand(UUID userId, UUID courseId) {}
