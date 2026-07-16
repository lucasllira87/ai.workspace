-- Drop learning tables from V007 that were superseded by the V012 schema.
-- V012 introduced courses/lessons/enrollments; the original study_materials/questions/flashcards
-- design was abandoned. Dropping here prevents confusion and reduces DB noise.
DROP TABLE IF EXISTS learning.flashcards;
DROP TABLE IF EXISTS learning.questions;
DROP TABLE IF EXISTS learning.summaries;
DROP TABLE IF EXISTS learning.study_materials;
