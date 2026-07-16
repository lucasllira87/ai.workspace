package com.aiworkspace.learning.presentation.request;

import com.aiworkspace.learning.domain.model.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCourseRequest(
        @NotBlank String title,
        String description,
        @NotNull CourseLevel level,
        @NotBlank String category
) {}
