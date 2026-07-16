package com.aiworkspace.notifications.application.dto;

import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationPreference;
import com.aiworkspace.notifications.domain.model.NotificationType;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record NotificationPreferenceDto(UUID userId,
                                         Map<String, Set<String>> settings,
                                         Instant updatedAt) {

    public static NotificationPreferenceDto from(NotificationPreference pref) {
        Map<String, Set<String>> mapped = pref.getSettings().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        e -> e.getValue().stream()
                                .map(NotificationChannel::name)
                                .collect(Collectors.toSet())
                ));
        return new NotificationPreferenceDto(pref.getUserId(), mapped, pref.getUpdatedAt());
    }
}
