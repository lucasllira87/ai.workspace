package com.aiworkspace.audit.application.dto;

import com.aiworkspace.audit.domain.model.AuditEvent;
import com.aiworkspace.audit.domain.model.AuditEventType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditEventDto(UUID id, UUID userId, AuditEventType eventType, String module,
                             String description, Map<String, String> metadata,
                             String ipAddress, Instant occurredAt) {

    public static AuditEventDto from(AuditEvent e) {
        return new AuditEventDto(e.getId(), e.getUserId(), e.getEventType(), e.getModule(),
                e.getDescription(), e.getMetadata(), e.getIpAddress(), e.getOccurredAt());
    }
}
