-- IAM Schema — Identity & Access Management

CREATE TABLE iam.users (
    id            UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE', 'BLOCKED', 'PENDING_VERIFICATION')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE iam.user_profiles (
    id                    UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id               UUID         NOT NULL,
    bio                   TEXT,
    avatar_url            VARCHAR(500),
    preferred_ai_provider VARCHAR(50),
    preferred_language    VARCHAR(10)  NOT NULL DEFAULT 'pt-BR',
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id)
        REFERENCES iam.users(id) ON DELETE CASCADE,
    CONSTRAINT uq_profile_user UNIQUE (user_id)
);

CREATE TABLE iam.user_roles (
    user_id    UUID        NOT NULL,
    role       VARCHAR(50) NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES iam.users(id) ON DELETE CASCADE
);

-- Refresh tokens: source of truth for active sessions.
-- Redis may be used as a read-through cache for performance, but PostgreSQL is authoritative.
CREATE TABLE iam.refresh_tokens (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    token        VARCHAR(255) NOT NULL,
    user_id      UUID         NOT NULL,
    device_name  VARCHAR(255),                  -- human-readable: "Chrome on MacBook"
    device_type  VARCHAR(50),                   -- WEB, MOBILE, DESKTOP, API
    ip_address   VARCHAR(45),
    expires_at   TIMESTAMPTZ  NOT NULL,
    last_used_at TIMESTAMPTZ,                   -- updated on each rotation
    revoked_at   TIMESTAMPTZ,                   -- NULL = active
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_refresh_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
        REFERENCES iam.users(id) ON DELETE CASCADE
);
