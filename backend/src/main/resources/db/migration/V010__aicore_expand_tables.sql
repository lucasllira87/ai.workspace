-- V010: Expand aicore tables for ai-core module

-- =====================================================
-- prompt_templates: add columns for full domain model
-- =====================================================
ALTER TABLE aicore.prompt_templates
    ADD COLUMN IF NOT EXISTS domain        VARCHAR(100)  NOT NULL DEFAULT 'general',
    ADD COLUMN IF NOT EXISTS variables     TEXT          NOT NULL DEFAULT '[]',
    ADD COLUMN IF NOT EXISTS updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now();

-- Ensure unique constraint on (name, version) if not present
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_prompt_templates_name_version'
          AND conrelid = 'aicore.prompt_templates'::regclass
    ) THEN
        ALTER TABLE aicore.prompt_templates
            ADD CONSTRAINT uq_prompt_templates_name_version UNIQUE (name, version);
    END IF;
END $$;

-- =====================================================
-- usage_metrics: full per-request tracking schema
-- =====================================================
ALTER TABLE aicore.usage_metrics
    ADD COLUMN IF NOT EXISTS module           VARCHAR(50),
    ADD COLUMN IF NOT EXISTS operation        VARCHAR(30),
    ADD COLUMN IF NOT EXISTS provider_id      VARCHAR(50),
    ADD COLUMN IF NOT EXISTS model_id         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS prompt_tokens    INT          NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS completion_tokens INT         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_tokens     INT          NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS estimated_cost   DECIMAL(10, 6),
    ADD COLUMN IF NOT EXISTS currency         VARCHAR(3)   NOT NULL DEFAULT 'USD',
    ADD COLUMN IF NOT EXISTS latency_ms       BIGINT,
    ADD COLUMN IF NOT EXISTS retry_count      INT          NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS success          BOOLEAN      NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS error_code       VARCHAR(50),
    ADD COLUMN IF NOT EXISTS requested_by     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS requested_at     TIMESTAMPTZ  NOT NULL DEFAULT now();

-- =====================================================
-- documents: add columns needed by DocumentIndexer
-- =====================================================
ALTER TABLE documents.documents
    ADD COLUMN IF NOT EXISTS chunk_count   INT,
    ADD COLUMN IF NOT EXISTS indexed_at    TIMESTAMPTZ;

-- =====================================================
-- Indexes for ai-core query patterns
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_prompt_templates_name_active
    ON aicore.prompt_templates (name) WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_prompt_templates_domain
    ON aicore.prompt_templates (domain);

CREATE INDEX IF NOT EXISTS idx_usage_metrics_requested_at
    ON aicore.usage_metrics (requested_at DESC);

CREATE INDEX IF NOT EXISTS idx_usage_metrics_provider_model
    ON aicore.usage_metrics (provider_id, model_id);

CREATE INDEX IF NOT EXISTS idx_usage_metrics_module
    ON aicore.usage_metrics (module, operation);
