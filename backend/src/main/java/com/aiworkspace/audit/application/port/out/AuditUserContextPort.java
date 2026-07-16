package com.aiworkspace.audit.application.port.out;

import java.util.UUID;

public interface AuditUserContextPort {
    UUID currentUserId();
}
