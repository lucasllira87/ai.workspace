-- Learning Platform schema

CREATE TABLE IF NOT EXISTS learning.courses (
    id               UUID PRIMARY KEY,
    owner_id         UUID NOT NULL,
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    level            VARCHAR(20) NOT NULL CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    category         VARCHAR(100) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                         CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS learning.lessons (
    id                  UUID PRIMARY KEY,
    course_id           UUID NOT NULL REFERENCES learning.courses(id) ON DELETE CASCADE,
    title               VARCHAR(200) NOT NULL,
    content             TEXT,
    video_url           VARCHAR(500),
    type                VARCHAR(20) NOT NULL CHECK (type IN ('TEXT', 'VIDEO', 'QUIZ')),
    lesson_order        INT NOT NULL,
    estimated_minutes   INT NOT NULL DEFAULT 10,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (course_id, lesson_order)
);

CREATE TABLE IF NOT EXISTS learning.quiz_questions (
    id                   UUID PRIMARY KEY,
    lesson_id            UUID NOT NULL REFERENCES learning.lessons(id) ON DELETE CASCADE,
    question             TEXT NOT NULL,
    options              JSONB NOT NULL,
    correct_option_index INT NOT NULL,
    explanation          TEXT
);

CREATE TABLE IF NOT EXISTS learning.enrollments (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL,
    course_id    UUID NOT NULL REFERENCES learning.courses(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                     CHECK (status IN ('ACTIVE', 'COMPLETED', 'DROPPED')),
    enrolled_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    UNIQUE (user_id, course_id)
);

CREATE TABLE IF NOT EXISTS learning.lesson_progress (
    id            UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES learning.enrollments(id) ON DELETE CASCADE,
    lesson_id     UUID NOT NULL REFERENCES learning.lessons(id),
    status        VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
                      CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
    started_at    TIMESTAMPTZ,
    completed_at  TIMESTAMPTZ,
    UNIQUE (enrollment_id, lesson_id)
);

CREATE TABLE IF NOT EXISTS learning.certificates (
    id               UUID PRIMARY KEY,
    enrollment_id    UUID NOT NULL REFERENCES learning.enrollments(id),
    user_id          UUID NOT NULL,
    course_id        UUID NOT NULL REFERENCES learning.courses(id),
    certificate_code VARCHAR(50) NOT NULL UNIQUE,
    issued_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_courses_owner_status    ON learning.courses (owner_id, status);
CREATE INDEX IF NOT EXISTS idx_courses_status          ON learning.courses (status);
CREATE INDEX IF NOT EXISTS idx_lessons_course          ON learning.lessons (course_id, lesson_order);
CREATE INDEX IF NOT EXISTS idx_enrollments_user        ON learning.enrollments (user_id, status);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_user ON learning.enrollments (course_id, user_id);
CREATE INDEX IF NOT EXISTS idx_lesson_progress_enrollment ON learning.lesson_progress (enrollment_id);
CREATE INDEX IF NOT EXISTS idx_certificates_user       ON learning.certificates (user_id);
