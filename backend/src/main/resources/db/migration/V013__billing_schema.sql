CREATE SCHEMA IF NOT EXISTS billing;

-- Plans (rarely mutated, seeded at startup)
CREATE TABLE billing.plans (
    id              UUID PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    price_amount    NUMERIC(10,2) NOT NULL,
    price_currency  VARCHAR(3) NOT NULL DEFAULT 'USD',
    billing_cycle   VARCHAR(20) NOT NULL,
    max_tokens_per_month   BIGINT NOT NULL,
    max_documents          INT NOT NULL,
    max_storage_bytes      BIGINT NOT NULL,
    max_enrollments        INT NOT NULL,
    features               TEXT[] NOT NULL DEFAULT '{}',
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_plans_billing_cycle CHECK (billing_cycle IN ('MONTHLY', 'YEARLY'))
);

-- Subscriptions (one active per user)
CREATE TABLE billing.subscriptions (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    plan_id         UUID NOT NULL REFERENCES billing.plans(id),
    status          VARCHAR(30) NOT NULL,
    external_id     VARCHAR(255),
    period_start    TIMESTAMPTZ NOT NULL,
    period_end      TIMESTAMPTZ NOT NULL,
    canceled_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_subscriptions_status
        CHECK (status IN ('TRIAL', 'ACTIVE', 'PAST_DUE', 'CANCELED', 'EXPIRED'))
);

CREATE UNIQUE INDEX idx_subscriptions_user_active
    ON billing.subscriptions(user_id)
    WHERE status IN ('TRIAL', 'ACTIVE', 'PAST_DUE');

CREATE INDEX idx_subscriptions_user ON billing.subscriptions(user_id);
CREATE INDEX idx_subscriptions_period_end ON billing.subscriptions(period_end)
    WHERE status IN ('TRIAL', 'ACTIVE');

-- Usage ledgers (one per user per billing period)
CREATE TABLE billing.usage_ledgers (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    period_start    TIMESTAMPTZ NOT NULL,
    period_end      TIMESTAMPTZ NOT NULL,
    total_tokens    BIGINT NOT NULL DEFAULT 0,
    total_documents INT NOT NULL DEFAULT 0,
    total_storage_bytes BIGINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usage_ledgers_user_period UNIQUE (user_id, period_start)
);

CREATE INDEX idx_usage_ledgers_user ON billing.usage_ledgers(user_id);

-- Usage entries (append-only log)
CREATE TABLE billing.usage_entries (
    id              UUID PRIMARY KEY,
    ledger_id       UUID NOT NULL REFERENCES billing.usage_ledgers(id),
    module          VARCHAR(50) NOT NULL,
    operation       VARCHAR(100) NOT NULL,
    token_count     BIGINT NOT NULL DEFAULT 0,
    document_count  INT NOT NULL DEFAULT 0,
    storage_bytes   BIGINT NOT NULL DEFAULT 0,
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usage_entries_ledger ON billing.usage_entries(ledger_id);
CREATE INDEX idx_usage_entries_recorded_at ON billing.usage_entries(recorded_at);

-- Invoices
CREATE TABLE billing.invoices (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    subscription_id UUID NOT NULL REFERENCES billing.subscriptions(id),
    period_start    TIMESTAMPTZ NOT NULL,
    period_end      TIMESTAMPTZ NOT NULL,
    total_amount    NUMERIC(10,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    status          VARCHAR(20) NOT NULL,
    external_id     VARCHAR(255),
    issued_at       TIMESTAMPTZ,
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_invoices_status CHECK (status IN ('DRAFT', 'ISSUED', 'PAID', 'VOID'))
);

CREATE INDEX idx_invoices_user ON billing.invoices(user_id);
CREATE INDEX idx_invoices_subscription ON billing.invoices(subscription_id);
CREATE UNIQUE INDEX idx_invoices_external ON billing.invoices(external_id)
    WHERE external_id IS NOT NULL;

-- Invoice line items
CREATE TABLE billing.invoice_line_items (
    id              UUID PRIMARY KEY,
    invoice_id      UUID NOT NULL REFERENCES billing.invoices(id),
    description     VARCHAR(255) NOT NULL,
    amount          NUMERIC(10,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    quantity        INT NOT NULL DEFAULT 1
);

CREATE INDEX idx_invoice_line_items_invoice ON billing.invoice_line_items(invoice_id);

-- Seed default plans
INSERT INTO billing.plans (id, name, price_amount, billing_cycle,
    max_tokens_per_month, max_documents, max_storage_bytes, max_enrollments, features) VALUES
(gen_random_uuid(), 'FREE',       0.00,  'MONTHLY', 50000,    5,   52428800,  3,  '{}'),
(gen_random_uuid(), 'PRO',        29.00, 'MONTHLY', 1000000,  50,  524288000, 20, '{AI_TUTOR,DOCUMENT_ASSISTANT}'),
(gen_random_uuid(), 'ENTERPRISE', 99.00, 'MONTHLY', 10000000, 500, 5368709120, 200, '{AI_TUTOR,DOCUMENT_ASSISTANT,CUSTOM_BRANDING}');
