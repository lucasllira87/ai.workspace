package com.aiworkspace.audit.application.command;

import com.aiworkspace.audit.domain.model.AuditEventType;

import java.util.Map;
import java.util.UUID;

public record RecordAuditEventCommand(UUID userId, AuditEventType eventType, String module,
                                       String description, Map<String, String> metadata) {

    public static RecordAuditEventCommand of(UUID userId, AuditEventType type, String module,
                                              String description) {
        return new RecordAuditEventCommand(userId, type, module, description, Map.of());
    }

    public static RecordAuditEventCommand of(UUID userId, AuditEventType type, String module,
                                              String description, Map<String, String> metadata) {
        return new RecordAuditEventCommand(userId, type, module, description, metadata);
    }
}
