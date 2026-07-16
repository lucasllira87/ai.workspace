package com.aiworkspace.learning.application.dto;

import java.util.UUID;

public record TutorAnswerDto(UUID lessonId, String question, String answer) {}
