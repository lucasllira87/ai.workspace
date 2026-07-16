package com.aiworkspace.notifications.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class NotificationPreference {

    private final UUID id;
    private final UUID userId;
    private final Map<NotificationType, Set<NotificationChannel>> settings;
    private Instant updatedAt;

    private NotificationPreference(UUID id, UUID userId,
                                    Map<NotificationType, Set<NotificationChannel>> settings,
                                    Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.settings = new EnumMap<>(settings);
        this.updatedAt = updatedAt;
    }

    public static NotificationPreference createDefault(UUID id, UUID userId) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(userId, "userId");
        // Opt-out model: all types enabled on all channels by default
        Map<NotificationType, Set<NotificationChannel>> defaults = new EnumMap<>(NotificationType.class);
        for (NotificationType type : NotificationType.values()) {
            defaults.put(type, EnumSet.allOf(NotificationChannel.class));
        }
        return new NotificationPreference(id, userId, defaults, Instant.now());
    }

    public static NotificationPreference reconstitute(UUID id, UUID userId,
                                                       Map<NotificationType, Set<NotificationChannel>> settings,
                                                       Instant updatedAt) {
        return new NotificationPreference(id, userId, settings, updatedAt);
    }

    public boolean isEnabled(NotificationType type, NotificationChannel channel) {
        Set<NotificationChannel> channels = settings.get(type);
        if (channels == null) return true; // default enabled
        return channels.contains(channel);
    }

    public void update(NotificationType type, Set<NotificationChannel> enabledChannels) {
        settings.put(type, EnumSet.copyOf(enabledChannels.isEmpty()
                ? EnumSet.noneOf(NotificationChannel.class)
                : enabledChannels));
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Map<NotificationType, Set<NotificationChannel>> getSettings() {
        return Collections.unmodifiableMap(settings);
    }
    public Instant getUpdatedAt() { return updatedAt; }
}
