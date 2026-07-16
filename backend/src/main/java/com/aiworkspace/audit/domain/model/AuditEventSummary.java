package com.aiworkspace.audit.domain.model;

import java.time.Instant;

public record AuditEventSummary(AuditEventType eventType, String module,
                                 String description, Instant occurredAt) {}
