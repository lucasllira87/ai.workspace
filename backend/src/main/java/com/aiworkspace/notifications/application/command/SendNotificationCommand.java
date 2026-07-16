package com.aiworkspace.notifications.application.command;

import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationType;

import java.util.Map;
import java.util.UUID;

public record SendNotificationCommand(UUID userId, NotificationType type,
                                       NotificationChannel channel, Map<String, Object> variables) {}
