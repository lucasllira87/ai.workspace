package com.aiworkspace.audit.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// Immutable aggregate — no state transitions, no domain events produced.
public class AuditEvent {

    private final UUID id;
    private final UUID userId;
    private final AuditEventType eventType;
    private final String module;
    private final String description;
    private final Map<String, String> metadata;
    private final String ipAddress;
    private final Instant occurredAt;

    private AuditEvent(UUID id, UUID userId, AuditEventType eventType, String module,
                       String description, Map<String, String> metadata, String ipAddress,
                       Instant occurredAt) {
        this.id = id;
        this.userId = userId;
        this.eventType = eventType;
        this.module = module;
        this.description = description;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
        this.ipAddress = ipAddress;
        this.occurredAt = occurredAt;
    }

    public static AuditEvent create(UUID userId, AuditEventType eventType, String module,
                                     String description, Map<String, String> metadata,
                                     String ipAddress) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(description, "description");
        return new AuditEvent(UUID.randomUUID(), userId, eventType, module,
                description, metadata, ipAddress, Instant.now());
    }

    public static AuditEvent reconstitute(UUID id, UUID userId, AuditEventType eventType,
                                           String module, String description,
                                           Map<String, String> metadata, String ipAddress,
                                           Instant occurredAt) {
        return new AuditEvent(id, userId, eventType, module, description, metadata,
                ipAddress, occurredAt);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public AuditEventType getEventType() { return eventType; }
    public String getModule() { return module; }
    public String getDescription() { return description; }
    public Map<String, String> getMetadata() { return Collections.unmodifiableMap(metadata); }
    public String getIpAddress() { return ipAddress; }
    public Instant getOccurredAt() { return occurredAt; }
}
