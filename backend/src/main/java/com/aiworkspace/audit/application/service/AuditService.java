package com.aiworkspace.audit.application.service;

import com.aiworkspace.audit.application.command.RecordAuditEventCommand;
import com.aiworkspace.audit.application.dto.AuditEventDto;
import com.aiworkspace.audit.application.port.in.GetAuditEventsUseCase;
import com.aiworkspace.audit.application.port.in.RecordAuditEventUseCase;
import com.aiworkspace.audit.application.port.out.AuditEventRepository;
import com.aiworkspace.audit.domain.model.AuditEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AuditService implements RecordAuditEventUseCase, GetAuditEventsUseCase {

    private final AuditEventRepository auditEventRepository;
    private final MeterRegistry meterRegistry;

    public AuditService(AuditEventRepository auditEventRepository, MeterRegistry meterRegistry) {
        this.auditEventRepository = auditEventRepository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void record(RecordAuditEventCommand command) {
        AuditEvent event = AuditEvent.create(command.userId(), command.eventType(),
                command.module(), command.description(), command.metadata(), null);
        auditEventRepository.save(event);
        Counter.builder("audit.events.recorded")
                .tag("module", command.module())
                .tag("type", command.eventType().name())
                .register(meterRegistry)
                .increment();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEventDto> getForUser(UUID userId, Pageable pageable) {
        return auditEventRepository.findByUserIdOrderByOccurredAtDesc(userId, pageable)
                .map(AuditEventDto::from);
    }
}
