package com.aiworkspace.aicore.infrastructure.metrics;

import com.aiworkspace.aicore.application.dto.UsageMetricEvent;
import com.aiworkspace.aicore.application.port.out.UsageMetricsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Component
public class JpaUsageMetricsAdapter implements UsageMetricsPort {

    private static final Logger log = LoggerFactory.getLogger(JpaUsageMetricsAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    public JpaUsageMetricsAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void record(UsageMetricEvent event) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO aicore.usage_metrics
                        (id, module, operation, provider_id, model_id,
                         prompt_tokens, completion_tokens, total_tokens,
                         estimated_cost, currency, latency_ms,
                         retry_count, success, error_code, requested_by, requested_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (id) DO NOTHING
                    """,
                    event.requestId(),
                    event.module(),
                    event.operation(),
                    event.providerId() != null ? event.providerId().value() : null,
                    event.modelId() != null ? event.modelId().value() : null,
                    event.promptTokens(),
                    event.completionTokens(),
                    event.totalTokens(),
                    event.estimatedCost() != null ? event.estimatedCost() : BigDecimal.ZERO,
                    event.currency(),
                    event.latencyMs(),
                    event.retryCount(),
                    event.success(),
                    event.errorCode(),
                    event.requestedBy(),
                    Timestamp.from(event.requestedAt()));
        } catch (Exception e) {
            // Metrics recording should never fail the main operation
            log.warn("Failed to record usage metric for operation '{}': {}", event.operation(), e.getMessage());
        }
    }
}
