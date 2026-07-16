package com.aiworkspace.notifications.infrastructure.persistence.repository;

import com.aiworkspace.notifications.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT n FROM NotificationJpaEntity n WHERE n.userId = :userId " +
           "AND n.channel = 'IN_APP' AND n.status = 'PENDING'")
    List<NotificationJpaEntity> findUnreadInAppByUserId(@Param("userId") UUID userId);
}
