package com.aiworkspace.audit.application;

import com.aiworkspace.audit.application.port.out.AuditEventRepository;
import com.aiworkspace.audit.application.port.out.BillingStatsPort;
import com.aiworkspace.audit.application.port.out.DocumentStatsPort;
import com.aiworkspace.audit.application.port.out.LearningStatsPort;
import com.aiworkspace.audit.application.service.DashboardService;
import com.aiworkspace.audit.domain.model.BillingStats;
import com.aiworkspace.audit.domain.model.DashboardStats;
import com.aiworkspace.audit.domain.model.DocumentStats;
import com.aiworkspace.audit.domain.model.LearningStats;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock DocumentStatsPort documentStats;
    @Mock LearningStatsPort learningStats;
    @Mock BillingStatsPort billingStats;
    @Mock AuditEventRepository auditEventRepository;

    SimpleMeterRegistry meterRegistry;
    DashboardService service;

    final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        // Direct executor: CompletableFuture tasks run synchronously in tests — no threading issues
        service = new DashboardService(documentStats, learningStats, billingStats,
                auditEventRepository, Executors.newSingleThreadExecutor(), meterRegistry);
    }

    @Test
    void getFor_returnsFullStatsWhenAllSourcesSucceed() {
        DocumentStats docs = new DocumentStats(10, 8, 1024 * 1024L);
        LearningStats learning = new LearningStats(3, 1, 2);
        BillingStats billing = new BillingStats("FREE", "TRIAL", 500L, 100_000L, 12L);

        when(documentStats.getStatsForUser(userId)).thenReturn(docs);
        when(learningStats.getStatsForUser(userId)).thenReturn(learning);
        when(billingStats.getStatsForUser(userId)).thenReturn(billing);
        when(auditEventRepository.findRecentByUserId(eq(userId), anyInt())).thenReturn(List.of());

        DashboardStats result = service.getFor(userId);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.documents().totalCount()).isEqualTo(10);
        assertThat(result.learning().completedCourses()).isEqualTo(1);
        assertThat(result.billing().planName()).isEqualTo("FREE");
    }

    @Test
    void getFor_returnsEmptyDocumentStatsOnDocumentPortFailure() {
        when(documentStats.getStatsForUser(userId)).thenThrow(new RuntimeException("DB timeout"));
        when(learningStats.getStatsForUser(userId)).thenReturn(LearningStats.empty());
        when(billingStats.getStatsForUser(userId)).thenReturn(BillingStats.unavailable());
        when(auditEventRepository.findRecentByUserId(eq(userId), anyInt())).thenReturn(List.of());

        DashboardStats result = service.getFor(userId);

        assertThat(result.documents()).isEqualTo(DocumentStats.empty());
        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    void getFor_incrementsFallbackCounterOnAnySourceFailure() {
        when(documentStats.getStatsForUser(userId)).thenThrow(new RuntimeException("timeout"));
        when(learningStats.getStatsForUser(userId)).thenThrow(new RuntimeException("timeout"));
        when(billingStats.getStatsForUser(userId)).thenReturn(BillingStats.unavailable());
        when(auditEventRepository.findRecentByUserId(any(), anyInt())).thenReturn(List.of());

        service.getFor(userId);

        Counter fallbacks = meterRegistry.counter("audit.dashboard.fallbacks");
        assertThat(fallbacks.count()).isEqualTo(2.0);
    }

    @Test
    void getFor_returnsBillingUnavailableOnBillingPortFailure() {
        when(documentStats.getStatsForUser(userId)).thenReturn(DocumentStats.empty());
        when(learningStats.getStatsForUser(userId)).thenReturn(LearningStats.empty());
        when(billingStats.getStatsForUser(userId)).thenThrow(new RuntimeException("billing down"));
        when(auditEventRepository.findRecentByUserId(any(), anyInt())).thenReturn(List.of());

        DashboardStats result = service.getFor(userId);

        assertThat(result.billing()).isEqualTo(BillingStats.unavailable());
        assertThat(meterRegistry.counter("audit.dashboard.fallbacks").count()).isEqualTo(1.0);
    }

    @Test
    void getFor_recordsTimerMetric() {
        when(documentStats.getStatsForUser(userId)).thenReturn(DocumentStats.empty());
        when(learningStats.getStatsForUser(userId)).thenReturn(LearningStats.empty());
        when(billingStats.getStatsForUser(userId)).thenReturn(BillingStats.unavailable());
        when(auditEventRepository.findRecentByUserId(any(), anyInt())).thenReturn(List.of());

        service.getFor(userId);

        assertThat(meterRegistry.timer("audit.dashboard.load").count()).isEqualTo(1L);
    }

    @Test
    void getFor_allSourcesFailStillReturnsResponse() {
        when(documentStats.getStatsForUser(userId)).thenThrow(new RuntimeException("doc down"));
        when(learningStats.getStatsForUser(userId)).thenThrow(new RuntimeException("learn down"));
        when(billingStats.getStatsForUser(userId)).thenThrow(new RuntimeException("billing down"));
        when(auditEventRepository.findRecentByUserId(any(), anyInt())).thenReturn(List.of());

        DashboardStats result = service.getFor(userId);

        assertThat(result).isNotNull();
        assertThat(result.documents()).isEqualTo(DocumentStats.empty());
        assertThat(result.learning()).isEqualTo(LearningStats.empty());
        assertThat(result.billing()).isEqualTo(BillingStats.unavailable());
        assertThat(meterRegistry.counter("audit.dashboard.fallbacks").count()).isEqualTo(3.0);
    }
}
