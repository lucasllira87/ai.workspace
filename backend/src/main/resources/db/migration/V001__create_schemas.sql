-- AI Workspace — Schema Setup
-- Each schema corresponds to a Bounded Context.
-- This separation mirrors DDD boundaries and enables future extraction to microservices.

CREATE SCHEMA IF NOT EXISTS iam;          -- Identity & Access Management
CREATE SCHEMA IF NOT EXISTS documents;    -- Document Intelligence
CREATE SCHEMA IF NOT EXISTS learning;     -- Learning Platform
CREATE SCHEMA IF NOT EXISTS aicore;       -- AI Core (cross-cutting AI infrastructure)
CREATE SCHEMA IF NOT EXISTS audit;        -- Audit & Compliance
CREATE SCHEMA IF NOT EXISTS finance;      -- Finance Manager (roadmap — boundary reserved)
CREATE SCHEMA IF NOT EXISTS code_review;  -- Code Reviewer (roadmap — boundary reserved)
