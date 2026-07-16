package com.aiworkspace.learning.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateLessonRequest(
        @NotBlank String title,
        String content,
        String videoUrl,
        @Min(1) int estimatedMinutes
) {}
