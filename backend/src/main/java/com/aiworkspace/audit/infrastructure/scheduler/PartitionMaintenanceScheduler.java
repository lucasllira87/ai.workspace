package com.aiworkspace.audit.infrastructure.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * Ensures an audit_events partition exists for next year before it is needed.
 * Runs monthly; PostgreSQL CREATE TABLE IF NOT EXISTS makes it idempotent.
 *
 * Gap: V015 migration ships partitions for 2026–2028 only.
 * Without this scheduler, inserts in 2029+ would fail with
 * "no partition of relation audit_events found for row".
 */
@Component
public class PartitionMaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(PartitionMaintenanceScheduler.class);

    private final JdbcTemplate jdbc;

    public PartitionMaintenanceScheduler(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Run at 03:00 on the 1st of every month
    @Scheduled(cron = "0 0 3 1 * *")
    public void ensureNextYearPartitionExists() {
        int nextYear = Year.now().getValue() + 1;
        String partitionName = "audit_events_" + nextYear;
        String fromDate = nextYear + "-01-01";
        String toDate = (nextYear + 1) + "-01-01";

        String sql = String.format("""
                CREATE TABLE IF NOT EXISTS audit.%s
                    PARTITION OF audit.audit_events
                    FOR VALUES FROM ('%s') TO ('%s')
                """, partitionName, fromDate, toDate);

        try {
            jdbc.execute(sql);
            log.info("Partition maintenance: ensured audit.{} exists", partitionName);
        } catch (Exception e) {
            log.error("Partition maintenance failed for {}: {}", partitionName, e.getMessage(), e);
        }
    }
}
