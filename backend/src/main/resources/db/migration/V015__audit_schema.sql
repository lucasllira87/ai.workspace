CREATE SCHEMA IF NOT EXISTS audit;

-- Partitioned by year (ADR-009 / ADR-032).
-- Each year's partition is created here for 2026-2028.
-- A DEFAULT partition captures any rows outside named ranges.
-- New year partitions must be created before each calendar year rollover.
CREATE TABLE audit.audit_events (
    id           UUID        NOT NULL DEFAULT uuid_generate_v4(),
    user_id      UUID        NOT NULL,
    event_type   VARCHAR(60) NOT NULL,
    module       VARCHAR(30) NOT NULL,
    description  TEXT        NOT NULL,
    metadata     JSONB       NOT NULL DEFAULT '{}',
    ip_address   VARCHAR(45),
    occurred_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (occurred_at);

CREATE TABLE audit.audit_events_2026
    PARTITION OF audit.audit_events
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

CREATE TABLE audit.audit_events_2027
    PARTITION OF audit.audit_events
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');

CREATE TABLE audit.audit_events_2028
    PARTITION OF audit.audit_events
    FOR VALUES FROM ('2028-01-01') TO ('2029-01-01');

-- Catch-all partition for data outside the named ranges (prevents insert failures)
CREATE TABLE audit.audit_events_default
    PARTITION OF audit.audit_events DEFAULT;

-- Primary key must include the partition key
ALTER TABLE audit.audit_events ADD PRIMARY KEY (id, occurred_at);

-- Covers list-by-user queries (main audit trail endpoint)
CREATE INDEX idx_audit_user_occurred
    ON audit.audit_events(user_id, occurred_at DESC);

-- Covers admin queries by module + type
CREATE INDEX idx_audit_module_type
    ON audit.audit_events(module, event_type, occurred_at DESC);
