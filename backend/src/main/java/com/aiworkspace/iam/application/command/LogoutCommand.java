package com.aiworkspace.iam.application.command;

public record LogoutCommand(String refreshToken, String authenticatedUserId) {}
