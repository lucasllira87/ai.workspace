package com.aiworkspace.notifications.application.port.in;

import com.aiworkspace.notifications.application.dto.NotificationPreferenceDto;

import java.util.UUID;

public interface GetPreferencesUseCase {
    NotificationPreferenceDto getForUser(UUID userId);
}
