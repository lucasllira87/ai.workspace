package com.aiworkspace.audit.infrastructure.persistence.adapter;

import com.aiworkspace.audit.application.port.out.AuditEventRepository;
import com.aiworkspace.audit.domain.model.AuditEvent;
import com.aiworkspace.audit.domain.model.AuditEventSummary;
import com.aiworkspace.audit.domain.model.AuditEventType;
import com.aiworkspace.audit.infrastructure.persistence.entity.AuditEventJpaEntity;
import com.aiworkspace.audit.infrastructure.persistence.repository.AuditEventJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aiworkspace.shared.exception.DomainException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AuditEventRepositoryAdapter implements AuditEventRepository {

    private final AuditEventJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public AuditEventRepositoryAdapter(AuditEventJpaRepository jpaRepository,
                                        ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(AuditEvent event) {
        jpaRepository.save(toEntity(event));
    }

    @Override
    public Page<AuditEvent> findByUserIdOrderByOccurredAtDesc(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserIdOrderByOccurredAtDesc(userId, pageable)
                .map(this::toDomain);
    }

    @Override
    public List<AuditEventSummary> findRecentByUserId(UUID userId, int limit) {
        Pageable page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "occurredAt"));
        return jpaRepository.findRecentByUserId(userId, page).stream()
                .map(e -> new AuditEventSummary(
                        AuditEventType.valueOf(e.getEventType()),
                        e.getModule(), e.getDescription(), e.getOccurredAt()))
                .collect(Collectors.toList());
    }

    private AuditEventJpaEntity toEntity(AuditEvent e) {
        AuditEventJpaEntity entity = new AuditEventJpaEntity();
        entity.setId(e.getId());
        entity.setUserId(e.getUserId());
        entity.setEventType(e.getEventType().name());
        entity.setModule(e.getModule());
        entity.setDescription(e.getDescription());
        entity.setMetadata(serializeMetadata(e.getMetadata()));
        entity.setIpAddress(e.getIpAddress());
        entity.setOccurredAt(e.getOccurredAt());
        return entity;
    }

    private AuditEvent toDomain(AuditEventJpaEntity e) {
        return AuditEvent.reconstitute(e.getId(), e.getUserId(),
                AuditEventType.valueOf(e.getEventType()),
                e.getModule(), e.getDescription(),
                deserializeMetadata(e.getMetadata()),
                e.getIpAddress(), e.getOccurredAt());
    }

    private String serializeMetadata(Map<String, String> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            throw new DomainException("Failed to serialize audit metadata");
        }
    }

    private Map<String, String> deserializeMetadata(String json) {
        try {
            if (json == null || json.isBlank()) return Map.of();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
