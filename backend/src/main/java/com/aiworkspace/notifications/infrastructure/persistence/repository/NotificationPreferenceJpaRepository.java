package com.aiworkspace.notifications.infrastructure.persistence.repository;

import com.aiworkspace.notifications.infrastructure.persistence.entity.NotificationPreferenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceJpaEntity, UUID> {

    Optional<NotificationPreferenceJpaEntity> findByUserId(UUID userId);
}
