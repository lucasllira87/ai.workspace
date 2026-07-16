package com.aiworkspace.audit.presentation;

import com.aiworkspace.audit.application.dto.AuditEventDto;
import com.aiworkspace.audit.application.port.in.GetAuditEventsUseCase;
import com.aiworkspace.audit.application.port.out.AuditUserContextPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final GetAuditEventsUseCase getAuditEvents;
    private final AuditUserContextPort userContext;

    public AuditController(GetAuditEventsUseCase getAuditEvents, AuditUserContextPort userContext) {
        this.getAuditEvents = getAuditEvents;
        this.userContext = userContext;
    }

    @GetMapping("/events")
    public ResponseEntity<Page<AuditEventDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = userContext.currentUserId();
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "occurredAt"));
        return ResponseEntity.ok(getAuditEvents.getForUser(userId, pageable));
    }
}
