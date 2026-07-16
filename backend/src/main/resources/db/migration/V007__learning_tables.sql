-- Learning Platform Schema

CREATE TABLE learning.study_materials (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id          UUID         NOT NULL,
    title             VARCHAR(500) NOT NULL,
    storage_reference VARCHAR(1000) NOT NULL,
    mime_type         VARCHAR(100) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'UPLOADED'
                      CHECK (status IN ('UPLOADED', 'PROCESSING', 'READY', 'FAILED')),
    page_count        INTEGER,
    detected_language VARCHAR(10),
    processed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE learning.summaries (
    id            UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id   UUID        NOT NULL,
    content       TEXT        NOT NULL,
    provider_used VARCHAR(50),
    model_used    VARCHAR(100),
    tokens_used   INTEGER,
    generated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_summary_material FOREIGN KEY (material_id)
        REFERENCES learning.study_materials(id) ON DELETE CASCADE,
    CONSTRAINT uq_summary_material UNIQUE (material_id)
);

CREATE TABLE learning.questions (
    id                   UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id          UUID        NOT NULL,
    stem                 TEXT        NOT NULL,
    options              JSONB       NOT NULL,
    correct_option_index INTEGER     NOT NULL,
    explanation          TEXT,
    difficulty           VARCHAR(10) CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    provider_used        VARCHAR(50),
    model_used           VARCHAR(100),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_question_material FOREIGN KEY (material_id)
        REFERENCES learning.study_materials(id) ON DELETE CASCADE
);

CREATE TABLE learning.flashcards (
    id            UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id   UUID        NOT NULL,
    front         TEXT        NOT NULL,
    back          TEXT        NOT NULL,
    provider_used VARCHAR(50),
    model_used    VARCHAR(100),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_flashcard_material FOREIGN KEY (material_id)
        REFERENCES learning.study_materials(id) ON DELETE CASCADE
);
