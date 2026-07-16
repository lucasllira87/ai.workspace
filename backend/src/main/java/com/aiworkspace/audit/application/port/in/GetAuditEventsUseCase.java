package com.aiworkspace.audit.application.port.in;

import com.aiworkspace.audit.application.dto.AuditEventDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetAuditEventsUseCase {
    Page<AuditEventDto> getForUser(UUID userId, Pageable pageable);
}
