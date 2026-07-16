package com.aiworkspace.notifications.infrastructure.persistence.adapter;

import com.aiworkspace.notifications.application.port.out.NotificationPreferenceRepository;
import com.aiworkspace.notifications.domain.model.NotificationChannel;
import com.aiworkspace.notifications.domain.model.NotificationPreference;
import com.aiworkspace.notifications.domain.model.NotificationType;
import com.aiworkspace.notifications.infrastructure.persistence.entity.NotificationPreferenceJpaEntity;
import com.aiworkspace.notifications.infrastructure.persistence.repository.NotificationPreferenceJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotificationPreferenceRepositoryAdapter implements NotificationPreferenceRepository {

    private final NotificationPreferenceJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public NotificationPreferenceRepositoryAdapter(NotificationPreferenceJpaRepository jpaRepository,
                                                    ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public NotificationPreference save(NotificationPreference pref) {
        NotificationPreferenceJpaEntity e = jpaRepository.findByUserId(pref.getUserId())
                .orElse(new NotificationPreferenceJpaEntity());
        e.setId(pref.getId());
        e.setUserId(pref.getUserId());
        e.setSettings(serializeSettings(pref.getSettings()));
        e.setUpdatedAt(pref.getUpdatedAt());
        jpaRepository.save(e);
        return pref;
    }

    @Override
    public Optional<NotificationPreference> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(this::toDomain);
    }

    private NotificationPreference toDomain(NotificationPreferenceJpaEntity e) {
        Map<NotificationType, Set<NotificationChannel>> settings = deserializeSettings(e.getSettings());
        return NotificationPreference.reconstitute(e.getId(), e.getUserId(), settings, e.getUpdatedAt());
    }

    private String serializeSettings(Map<NotificationType, Set<NotificationChannel>> settings) {
        try {
            Map<String, List<String>> raw = settings.entrySet().stream()
                    .collect(Collectors.toMap(
                            en -> en.getKey().name(),
                            en -> en.getValue().stream().map(NotificationChannel::name).collect(Collectors.toList())
                    ));
            return objectMapper.writeValueAsString(raw);
        } catch (Exception ex) {
            throw new DomainException("Failed to serialize notification preferences");
        }
    }

    private Map<NotificationType, Set<NotificationChannel>> deserializeSettings(String json) {
        try {
            Map<String, List<String>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            Map<NotificationType, Set<NotificationChannel>> result = new EnumMap<>(NotificationType.class);
            for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
                NotificationType type = NotificationType.valueOf(entry.getKey());
                Set<NotificationChannel> channels = entry.getValue().stream()
                        .map(NotificationChannel::valueOf)
                        .collect(Collectors.toCollection(() -> EnumSet.noneOf(NotificationChannel.class)));
                result.put(type, channels);
            }
            return result;
        } catch (Exception ex) {
            throw new DomainException("Failed to deserialize notification preferences");
        }
    }
}
