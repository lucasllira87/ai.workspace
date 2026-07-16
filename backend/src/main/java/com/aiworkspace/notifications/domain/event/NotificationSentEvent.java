package com.aiworkspace.notifications.domain.event;

import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationSentEvent(UUID notificationId, UUID userId,
                                     NotificationType type, NotificationChannel channel,
                                     Instant occurredAt) {}
