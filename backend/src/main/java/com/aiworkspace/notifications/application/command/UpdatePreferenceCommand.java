package com.aiworkspace.notifications.application.command;

import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationType;

import java.util.Set;
import java.util.UUID;

public record UpdatePreferenceCommand(UUID userId, NotificationType type,
                                       Set<NotificationChannel> enabledChannels) {}
