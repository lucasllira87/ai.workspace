package com.aiworkspace.iam.application.dto;

import java.util.Set;

public record UserDto(String id, String email, String fullName, Set<String> roles, String status) {}
