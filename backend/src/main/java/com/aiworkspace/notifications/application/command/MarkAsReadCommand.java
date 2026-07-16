package com.aiworkspace.notifications.application.command;

import java.util.UUID;

public record MarkAsReadCommand(UUID notificationId, UUID userId) {}
