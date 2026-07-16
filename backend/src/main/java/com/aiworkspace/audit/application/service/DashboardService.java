package com.aiworkspace.audit.application.service;

import com.aiworkspace.audit.application.port.in.GetDashboardStatsUseCase;
import com.aiworkspace.audit.application.port.out.AuditEventRepository;
import com.aiworkspace.audit.application.port.out.BillingStatsPort;
import com.aiworkspace.audit.application.port.out.DocumentStatsPort;
import com.aiworkspace.audit.application.port.out.LearningStatsPort;
import com.aiworkspace.audit.domain.model.BillingStats;
import com.aiworkspace.audit.domain.model.DashboardStats;
import com.aiworkspace.audit.domain.model.DocumentStats;
import com.aiworkspace.audit.domain.model.LearningStats;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

// No class-level @Transactional: each CompletableFuture task runs on its own thread and
// cannot participate in a ThreadLocal-bound Spring transaction. Each JPA repository call
// manages its own short-lived read-only transaction via Spring Data defaults.
@Service
public class DashboardService implements GetDashboardStatsUseCase {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final int RECENT_ACTIVITY_LIMIT = 10;

    private final DocumentStatsPort documentStats;
    private final LearningStatsPort learningStats;
    private final BillingStatsPort billingStats;
    private final AuditEventRepository auditEventRepository;
    private final Executor asyncExecutor;
    private final Timer dashboardTimer;
    private final Counter fallbackCounter;

    public DashboardService(DocumentStatsPort documentStats,
                             LearningStatsPort learningStats,
                             BillingStatsPort billingStats,
                             AuditEventRepository auditEventRepository,
                             @Qualifier("auditAsyncExecutor") Executor asyncExecutor,
                             MeterRegistry meterRegistry) {
        this.documentStats = documentStats;
        this.learningStats = learningStats;
        this.billingStats = billingStats;
        this.auditEventRepository = auditEventRepository;
        this.asyncExecutor = asyncExecutor;
        this.dashboardTimer = Timer.builder("audit.dashboard.load")
                .description("Time to aggregate all dashboard stats for a user")
                .register(meterRegistry);
        this.fallbackCounter = Counter.builder("audit.dashboard.fallbacks")
                .description("Number of times a stats source returned a fallback/empty value")
                .register(meterRegistry);
    }

    @Override
    public DashboardStats getFor(UUID userId) {
        return dashboardTimer.record(() -> aggregateStats(userId));
    }

    private DashboardStats aggregateStats(UUID userId) {
        CompletableFuture<DocumentStats> docFuture =
                CompletableFuture.supplyAsync(() -> safeDocStats(userId), asyncExecutor);
        CompletableFuture<LearningStats> learnFuture =
                CompletableFuture.supplyAsync(() -> safeLearnStats(userId), asyncExecutor);
        CompletableFuture<BillingStats> billFuture =
                CompletableFuture.supplyAsync(() -> safeBillStats(userId), asyncExecutor);

        CompletableFuture.allOf(docFuture, learnFuture, billFuture).join();

        var recentActivity = auditEventRepository.findRecentByUserId(userId, RECENT_ACTIVITY_LIMIT);

        return DashboardStats.of(userId, docFuture.join(), learnFuture.join(),
                billFuture.join(), recentActivity);
    }

    private DocumentStats safeDocStats(UUID userId) {
        try {
            return documentStats.getStatsForUser(userId);
        } catch (Exception e) {
            log.warn("Document stats unavailable for userId={}: {}", userId, e.getMessage());
            fallbackCounter.increment();
            return DocumentStats.empty();
        }
    }

    private LearningStats safeLearnStats(UUID userId) {
        try {
            return learningStats.getStatsForUser(userId);
        } catch (Exception e) {
            log.warn("Learning stats unavailable for userId={}: {}", userId, e.getMessage());
            fallbackCounter.increment();
            return LearningStats.empty();
        }
    }

    private BillingStats safeBillStats(UUID userId) {
        try {
            return billingStats.getStatsForUser(userId);
        } catch (Exception e) {
            log.warn("Billing stats unavailable for userId={}: {}", userId, e.getMessage());
            fallbackCounter.increment();
            return BillingStats.unavailable();
        }
    }
}
