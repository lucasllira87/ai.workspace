package com.aiworkspace.notifications.application.port.out;

import java.util.UUID;

public interface NotificationUserContextPort {
    UUID currentUserId();
    String emailForUser(UUID userId);
}
