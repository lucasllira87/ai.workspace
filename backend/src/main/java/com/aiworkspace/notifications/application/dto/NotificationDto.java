package com.aiworkspace.notifications.application.dto;

import com.aiworkspace.notifications.domain.model.Notification;
import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationStatus;
import com.aiworkspace.notifications.domain.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(UUID id, UUID userId, NotificationType type, NotificationChannel channel,
                               String subject, String body, NotificationStatus status,
                               Instant sentAt, Instant readAt, Instant createdAt) {

    public static NotificationDto from(Notification n) {
        return new NotificationDto(n.getId(), n.getUserId(), n.getType(), n.getChannel(),
                n.getSubject(), n.getBody(), n.getStatus(),
                n.getSentAt(), n.getReadAt(), n.getCreatedAt());
    }
}
