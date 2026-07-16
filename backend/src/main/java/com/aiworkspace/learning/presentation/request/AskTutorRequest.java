package com.aiworkspace.learning.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record AskTutorRequest(
        @NotBlank String question
) {}
