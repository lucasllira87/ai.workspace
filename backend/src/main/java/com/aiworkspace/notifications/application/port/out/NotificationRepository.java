package com.aiworkspace.notifications.application.port.out;

import com.aiworkspace.notifications.domain.model.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Notification> findUnreadInAppByUserId(UUID userId);
}
