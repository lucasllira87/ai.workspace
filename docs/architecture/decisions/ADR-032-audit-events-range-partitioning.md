# ADR-032 — Range Partitioning for `audit_events` by Year

**Status:** Accepted  
**Date:** 2026-07-15  
**Module:** Audit / Dashboard

---

## Context

Audit events accumulate continuously: every domain event from every module produces at least one audit row. In 3 years of operation at expected scale (~100 users, ~50 domain events/user/day), the table could reach 5–6 million rows. Without structural mitigation, unbounded table growth degrades index scans and makes data retention management (e.g., purge records older than 3 years) expensive — a DELETE on 5M rows locks the table.

## Decision

Use **PostgreSQL declarative range partitioning** on `occurred_at` by year.

```sql
CREATE TABLE audit.audit_events (...)
    PARTITION BY RANGE (occurred_at);

CREATE TABLE audit.audit_events_2026
    PARTITION OF audit.audit_events
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
```

Consequences of this choice:
- **Composite PK required:** PostgreSQL mandates the partition key be part of the primary key. PK becomes `(id, occurred_at)`, requiring an `@IdClass(AuditEventId.class)` JPA mapping.
- **Partition pruning:** queries filtered on `occurred_at` (e.g., `WHERE occurred_at >= '2026-01-01'`) skip irrelevant partitions entirely — O(months) vs O(total rows).
- **Cheap data lifecycle:** dropping an old year is `DROP TABLE audit.audit_events_20XX` — near-instant with no table lock.

## Automated partition maintenance

V015 migration ships 2026, 2027, 2028 partitions. `PartitionMaintenanceScheduler` runs monthly via `@Scheduled(cron = "0 0 3 1 * *")` and issues `CREATE TABLE IF NOT EXISTS` for next year's partition — idempotent, no risk of duplicate creation.

## Alternatives Considered

| Option | Reason Rejected |
|---|---|
| Single unpartitioned table + periodic VACUUM | Acceptable short-term; becomes a problem at 10M+ rows; no cheap lifecycle |
| Time-series DB (TimescaleDB) | Additional infrastructure dependency; overkill for MVP scale |
| Monthly partitions | More granularity than needed; higher JPA mapping complexity |

## Consequences

- JPA entities require `@IdClass` with a serializable composite key class — slightly more boilerplate.
- Cross-year queries (e.g., audit trail spanning Dec 31 → Jan 1) hit 2 partitions — PostgreSQL handles this transparently.
- Partition maintenance must run before the first insert of a new year; `PartitionMaintenanceScheduler` creates next year's partition every December 1 at minimum.
