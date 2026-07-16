package com.aiworkspace.iam.application.command;

public record RefreshTokenCommand(String refreshToken, String ipAddress) {}
