package com.aiworkspace.iam.application.dto;

public record AuthTokenDto(String accessToken, String refreshToken, UserDto user) {}
