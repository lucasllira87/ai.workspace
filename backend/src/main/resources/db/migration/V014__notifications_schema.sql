CREATE SCHEMA IF NOT EXISTS notifications;

CREATE TABLE notifications.notifications (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    type            VARCHAR(60) NOT NULL,
    channel         VARCHAR(20) NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    body            TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL,
    failed_reason   TEXT,
    sent_at         TIMESTAMPTZ,
    read_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_notifications_channel CHECK (channel IN ('EMAIL', 'IN_APP')),
    CONSTRAINT chk_notifications_status  CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ'))
);

-- Covers list-all-by-user (no channel filter)
CREATE INDEX idx_notifications_user_created
    ON notifications.notifications(user_id, created_at DESC);

-- Covers list-by-user-and-channel (e.g., future per-channel inbox)
CREATE INDEX idx_notifications_user_channel
    ON notifications.notifications(user_id, channel, created_at DESC);

-- Partial index for the unread IN_APP query — used by findUnreadInAppByUserId
CREATE INDEX idx_notifications_user_unread
    ON notifications.notifications(user_id, status)
    WHERE status IN ('PENDING', 'SENT') AND channel = 'IN_APP';

CREATE TABLE notifications.preferences (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL UNIQUE,
    settings    JSONB NOT NULL DEFAULT '{}',
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_preferences_user ON notifications.preferences(user_id);
