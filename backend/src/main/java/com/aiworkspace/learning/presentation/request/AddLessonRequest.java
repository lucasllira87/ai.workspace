package com.aiworkspace.learning.presentation.request;

import com.aiworkspace.learning.domain.model.LessonType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddLessonRequest(
        @NotBlank String title,
        String content,
        String videoUrl,
        @NotNull LessonType type,
        @Min(1) int estimatedMinutes
) {}
