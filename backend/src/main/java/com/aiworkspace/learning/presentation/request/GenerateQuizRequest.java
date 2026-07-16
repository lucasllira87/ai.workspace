package com.aiworkspace.learning.presentation.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GenerateQuizRequest(
        @Min(1) @Max(20) int questionCount
) {}
