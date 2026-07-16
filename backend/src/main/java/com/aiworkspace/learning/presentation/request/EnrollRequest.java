package com.aiworkspace.learning.presentation.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnrollRequest(
        @NotNull UUID courseId
) {}
