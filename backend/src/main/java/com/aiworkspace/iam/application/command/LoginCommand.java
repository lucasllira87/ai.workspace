package com.aiworkspace.iam.application.command;

public record LoginCommand(
        String email,
        String password,
        String deviceName,
        String deviceType,
        String ipAddress
) {}
