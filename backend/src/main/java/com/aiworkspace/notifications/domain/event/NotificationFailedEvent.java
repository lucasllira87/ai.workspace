package com.aiworkspace.notifications.domain.event;

import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationFailedEvent(UUID notificationId, UUID userId,
                                       NotificationType type, NotificationChannel channel,
                                       String reason, Instant occurredAt) {}
