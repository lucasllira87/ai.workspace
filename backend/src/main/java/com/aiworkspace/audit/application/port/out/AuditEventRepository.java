package com.aiworkspace.audit.application.port.out;

import com.aiworkspace.audit.domain.model.AuditEvent;
import com.aiworkspace.audit.domain.model.AuditEventSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository {
    void save(AuditEvent event);
    Page<AuditEvent> findByUserIdOrderByOccurredAtDesc(UUID userId, Pageable pageable);
    List<AuditEventSummary> findRecentByUserId(UUID userId, int limit);
}
