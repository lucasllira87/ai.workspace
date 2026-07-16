package com.aiworkspace.learning.presentation.response;

import java.util.UUID;

public record TutorResponse(UUID lessonId, String question, String answer) {}
