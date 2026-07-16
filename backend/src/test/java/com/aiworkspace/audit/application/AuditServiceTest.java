package com.aiworkspace.audit.application;

import com.aiworkspace.audit.application.command.RecordAuditEventCommand;
import com.aiworkspace.audit.application.dto.AuditEventDto;
import com.aiworkspace.audit.application.port.out.AuditEventRepository;
import com.aiworkspace.audit.application.service.AuditService;
import com.aiworkspace.audit.domain.model.AuditEvent;
import com.aiworkspace.audit.domain.model.AuditEventType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock AuditEventRepository auditEventRepository;

    SimpleMeterRegistry meterRegistry;
    AuditService service;

    final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new AuditService(auditEventRepository, meterRegistry);
    }

    @Test
    void record_savesAuditEventToRepository() {
        RecordAuditEventCommand command = RecordAuditEventCommand.of(
                userId, AuditEventType.USER_REGISTERED, "iam",
                "User registered with email test@example.com",
                Map.of("email", "test@example.com"));

        service.record(command);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        AuditEvent saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getEventType()).isEqualTo(AuditEventType.USER_REGISTERED);
        assertThat(saved.getModule()).isEqualTo("iam");
        assertThat(saved.getDescription()).contains("test@example.com");
    }

    @Test
    void record_incrementsCounterWithModuleAndTypeTags() {
        RecordAuditEventCommand command = RecordAuditEventCommand.of(
                userId, AuditEventType.DOCUMENT_INDEXED, "documents",
                "Document indexed", Map.of());

        service.record(command);

        Counter counter = meterRegistry.counter("audit.events.recorded",
                "module", "documents", "type", "DOCUMENT_INDEXED");
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void record_usesImmutableAuditEventCreation() {
        RecordAuditEventCommand command = RecordAuditEventCommand.of(
                userId, AuditEventType.PAYMENT_FAILED, "billing",
                "Payment failed", Map.of("invoiceId", "inv-123"));

        service.record(command);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());

        AuditEvent saved = captor.getValue();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOccurredAt()).isNotNull();
        assertThat(saved.getOccurredAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(saved.getMetadata()).containsEntry("invoiceId", "inv-123");
    }

    @Test
    void getForUser_returnsPageFromRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        AuditEvent event = AuditEvent.create(userId, AuditEventType.USER_REGISTERED,
                "iam", "desc", Map.of(), null);
        when(auditEventRepository.findByUserIdOrderByOccurredAtDesc(eq(userId), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(event)));

        var page = service.getForUser(userId, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0)).isInstanceOf(AuditEventDto.class);
    }

    @Test
    void record_differentModulesIncrementSeparateCounters() {
        service.record(RecordAuditEventCommand.of(userId,
                AuditEventType.DOCUMENT_INDEXED, "documents", "d", Map.of()));
        service.record(RecordAuditEventCommand.of(userId,
                AuditEventType.CERTIFICATE_ISSUED, "learning", "l", Map.of()));

        assertThat(meterRegistry.counter("audit.events.recorded",
                "module", "documents", "type", "DOCUMENT_INDEXED").count()).isEqualTo(1.0);
        assertThat(meterRegistry.counter("audit.events.recorded",
                "module", "learning", "type", "CERTIFICATE_ISSUED").count()).isEqualTo(1.0);
    }
}
