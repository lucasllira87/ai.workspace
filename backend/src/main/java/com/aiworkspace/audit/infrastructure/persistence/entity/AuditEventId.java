package com.aiworkspace.audit.infrastructure.persistence.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

// Composite PK for the partitioned audit_events table: (id, occurred_at)
public class AuditEventId implements Serializable {

    private UUID id;
    private Instant occurredAt;

    public AuditEventId() {}

    public AuditEventId(UUID id, Instant occurredAt) {
        this.id = id;
        this.occurredAt = occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditEventId that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, occurredAt);
    }
}
