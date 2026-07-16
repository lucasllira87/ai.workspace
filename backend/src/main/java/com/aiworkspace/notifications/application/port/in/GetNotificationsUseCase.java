package com.aiworkspace.notifications.application.port.in;

import com.aiworkspace.notifications.application.dto.NotificationDto;

import java.util.List;
import java.util.UUID;

public interface GetNotificationsUseCase {
    List<NotificationDto> getForUser(UUID userId);
    List<NotificationDto> getUnreadForUser(UUID userId);
}
