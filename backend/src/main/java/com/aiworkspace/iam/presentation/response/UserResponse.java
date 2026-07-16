package com.aiworkspace.iam.presentation.response;

import java.util.Set;

public record UserResponse(
        String id,
        String email,
        String fullName,
        Set<String> roles,
        String status
) {}
