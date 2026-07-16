package com.aiworkspace.notifications.infrastructure.persistence.adapter;

import com.aiworkspace.notifications.application.port.out.NotificationRepository;
import com.aiworkspace.notifications.domain.model.Notification;
import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationStatus;
import com.aiworkspace.notifications.domain.model.NotificationType;
import com.aiworkspace.notifications.infrastructure.persistence.entity.NotificationJpaEntity;
import com.aiworkspace.notifications.infrastructure.persistence.repository.NotificationJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository,
                                          ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Notification save(Notification notification) {
        jpaRepository.save(toEntity(notification));
        // BUG-1 pattern: publish domain events from the original aggregate, not from a reconstituted copy
        notification.getDomainEvents().forEach(eventPublisher::publishEvent);
        notification.clearDomainEvents();
        return notification;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId) {
        return jpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Notification> findUnreadInAppByUserId(UUID userId) {
        return jpaRepository.findUnreadInAppByUserId(userId)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    private NotificationJpaEntity toEntity(Notification n) {
        NotificationJpaEntity e = new NotificationJpaEntity();
        e.setId(n.getId());
        e.setUserId(n.getUserId());
        e.setType(n.getType().name());
        e.setChannel(n.getChannel().name());
        e.setSubject(n.getSubject());
        e.setBody(n.getBody());
        e.setStatus(n.getStatus().name());
        e.setFailedReason(n.getFailedReason());
        e.setSentAt(n.getSentAt());
        e.setReadAt(n.getReadAt());
        e.setCreatedAt(n.getCreatedAt());
        return e;
    }

    private Notification toDomain(NotificationJpaEntity e) {
        return Notification.reconstitute(e.getId(), e.getUserId(),
                NotificationType.valueOf(e.getType()),
                NotificationChannel.valueOf(e.getChannel()),
                e.getSubject(), e.getBody(),
                NotificationStatus.valueOf(e.getStatus()),
                e.getFailedReason(), e.getSentAt(), e.getReadAt(), e.getCreatedAt());
    }
}
