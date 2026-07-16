-- AI Core Schema — Cross-cutting AI infrastructure

-- Versioned prompt templates used by the system.
-- Versioning allows A/B testing and safe rollback of prompt changes.
CREATE TABLE aicore.prompt_templates (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    content      TEXT         NOT NULL,
    version      INTEGER      NOT NULL DEFAULT 1,
    module       VARCHAR(50)  NOT NULL,   -- documents, learning, etc.
    use_case     VARCHAR(100) NOT NULL,   -- summarize, generate_questions, chat, etc.
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by   UUID,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_template_name_version UNIQUE (name, version)
);

-- Configuration for each supported AI provider.
-- Allows enabling/disabling providers and configuring defaults per use case.
CREATE TABLE aicore.provider_configs (
    id             UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_name  VARCHAR(50)  NOT NULL,   -- claude, openai, gemini
    model_name     VARCHAR(100) NOT NULL,
    use_case       VARCHAR(100) NOT NULL,   -- chat, summarize, generate_questions, embed
    max_tokens     INTEGER,
    temperature    DECIMAL(3,2),
    is_enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    priority       INTEGER      NOT NULL DEFAULT 1,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_provider_use_case UNIQUE (provider_name, model_name, use_case)
);

-- Per-request AI usage metrics for monitoring and cost control.
CREATE TABLE aicore.usage_metrics (
    id               UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          UUID          NOT NULL,
    module           VARCHAR(50)   NOT NULL,
    use_case         VARCHAR(100)  NOT NULL,
    provider         VARCHAR(50)   NOT NULL,
    model            VARCHAR(100)  NOT NULL,
    prompt_tokens    INTEGER,
    completion_tokens INTEGER,
    total_tokens     INTEGER,
    estimated_cost   DECIMAL(10,8),
    latency_ms       BIGINT,
    status           VARCHAR(20)   NOT NULL CHECK (status IN ('SUCCESS', 'ERROR', 'TIMEOUT')),
    error_message    TEXT,
    resource_id      VARCHAR(255),    -- document_id, material_id, etc.
    resource_type    VARCHAR(50),
    occurred_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
