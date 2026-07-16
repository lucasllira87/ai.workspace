# Índice de ADRs — AI Workspace

Todas as decisões arquiteturais registradas do projeto, em ordem cronológica.

> **Convenção de status:**
> - ✅ Accepted — decisão vigente
> - ⚠️ Deprecated — substituída por ADR mais recente
> - 🔄 Superseded — indicado qual ADR a substitui

---

## Fundação & Estilo Arquitetural

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-001](decisions/ADR-001-modular-monolith.md) | Modular Monolith over Microservices | ✅ | 2026-06-01 |
| [ADR-002](decisions/ADR-002-clean-architecture-ddd.md) | Clean Architecture + DDD por módulo | ✅ | 2026-06-01 |
| [ADR-003](decisions/ADR-003-spring-boot-3.md) | Spring Boot 3.3 como framework de aplicação | ✅ | 2026-06-01 |
| [ADR-004](decisions/ADR-004-java-21-virtual-threads.md) | Java 21 com Virtual Threads (Project Loom) | ✅ | 2026-06-01 |

## Persistência

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-005](decisions/ADR-005-postgresql-primary-db.md) | PostgreSQL 16 como banco de dados principal | ✅ | 2026-06-02 |
| [ADR-006](decisions/ADR-006-pgvector-embeddings.md) | pgvector para armazenamento de embeddings | ✅ | 2026-06-02 |
| [ADR-007](decisions/ADR-007-redis-blacklist-cache.md) | Redis para blacklist de tokens JWT e cache | ✅ | 2026-06-02 |
| [ADR-008](decisions/ADR-008-flyway-migrations.md) | Flyway para migrações de banco de dados | ✅ | 2026-06-02 |
| [ADR-032](decisions/ADR-032-audit-events-range-partitioning.md) | Range partitioning anual em `audit_events` + PK composta + scheduler | ✅ | 2026-07-14 |

## Segurança & Autenticação

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-009](decisions/ADR-009-jwt-auth.md) | JWT com access token (15 min) + refresh token (7 dias) | ✅ | 2026-06-03 |

## Domain Model & Padrões

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-010](decisions/ADR-010-domain-events-after-commit.md) | Domain Events via `@TransactionalEventListener(AFTER_COMMIT)` | ✅ | 2026-06-04 |
| [ADR-011](decisions/ADR-011-async-cross-module-listeners.md) | `@Async` obrigatório em todos os listeners cross-module | ✅ | 2026-06-04 |
| [ADR-012](decisions/ADR-012-module-layer-structure.md) | Estrutura de 4 camadas por módulo (domain/application/infrastructure/presentation) | ✅ | 2026-06-04 |
| [ADR-029](decisions/ADR-029-bug1-domain-event-publishing.md) | BUG-1 pattern — publicar domain events a partir do agregado original, não do retorno de `save()` | ✅ | 2026-07-10 |
| [ADR-030](decisions/ADR-030-aggregate-root-base-class.md) | `AggregateRoot<T>` como classe base com suporte a domain events | ✅ | 2026-07-10 |
| [ADR-031](decisions/ADR-031-shared-value-objects.md) | Value objects compartilhados: `Money`, `BillingCycle`, `PlanQuota` | ✅ | 2026-07-10 |

## Módulos de Negócio

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-013](decisions/ADR-013-auth-module.md) | Design do módulo `auth` — registro, login, refresh, blacklist | ✅ | 2026-06-05 |
| [ADR-014](decisions/ADR-014-billing-module.md) | Design do módulo `billing` — planos, assinaturas, trial, cotas | ✅ | 2026-06-06 |
| [ADR-015](decisions/ADR-015-documents-module.md) | Design do módulo `documents` — upload, extração, RAG pipeline | ✅ | 2026-06-07 |
| [ADR-016](decisions/ADR-016-learning-module.md) | Design do módulo `learning` — cursos, matrículas, progresso | ✅ | 2026-06-08 |
| [ADR-017](decisions/ADR-017-notifications-module.md) | Design do módulo `notifications` — e-mail + in-app + SSE | ✅ | 2026-06-09 |
| [ADR-018](decisions/ADR-018-audit-module.md) | Design do módulo `audit` — eventos de auditoria + dashboard | ✅ | 2026-06-10 |

## AI & RAG

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-019](decisions/ADR-019-ai-provider-abstraction.md) | Abstração de AI providers (OpenAI, Anthropic, Google, Ollama) via port | ✅ | 2026-06-11 |
| [ADR-020](decisions/ADR-020-rag-pipeline.md) | Pipeline RAG: chunking → embedding → indexação → retrieval | ✅ | 2026-06-11 |
| [ADR-021](decisions/ADR-021-resilience4j-ai.md) | Resilience4j para circuit breaker e retry em chamadas AI | ✅ | 2026-06-12 |
| [ADR-022](decisions/ADR-022-sse-document-chat.md) | SSE para streaming de respostas de chat de documento | ✅ | 2026-06-12 |

## API & Infraestrutura de Aplicação

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-023](decisions/ADR-023-global-exception-handler.md) | `GlobalExceptionHandler` com `@RestControllerAdvice` | ✅ | 2026-06-13 |
| [ADR-024](decisions/ADR-024-openapi-springdoc.md) | Springdoc / OpenAPI 3 para documentação de API | ✅ | 2026-06-13 |
| [ADR-025](decisions/ADR-025-hikaricp-config.md) | Configuração do pool HikariCP (10 max, 2 min-idle) | ✅ | 2026-06-14 |
| [ADR-026](decisions/ADR-026-redis-lettuce-pool.md) | Pool Lettuce para conexões Redis (8 max-active) | ✅ | 2026-06-14 |
| [ADR-027](decisions/ADR-027-multipart-limits.md) | Limites de upload multipart: 50 MB por arquivo, 55 MB por request | ✅ | 2026-06-14 |
| [ADR-028](decisions/ADR-028-mail-smtp-mailgun.md) | Envio de e-mail via SMTP (Mailgun) com STARTTLS | ✅ | 2026-06-15 |

## Dashboard

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-033](decisions/ADR-033-cross-module-jpa-reads-for-dashboard.md) | Leituras JPA cross-module diretas para `DashboardService` | ✅ | 2026-07-14 |
| [ADR-034](decisions/ADR-034-parallel-dashboard-completablefuture.md) | `CompletableFuture.allOf()` + Virtual Threads para stats paralelas | ✅ | 2026-07-14 |

## Testes & Qualidade

> A estratégia de testes foi documentada na revisão formal da Fase 6 — sem ADR separado.
> Ver: [docs/backend/phase6-tests-review.md](../backend/phase6-tests-review.md)
>
> Decisões-chave: Testcontainers singleton com `.withReuse(true)`, ArchUnit com 6 regras arquiteturais, JaCoCo com threshold ≥ 80% em linhas.

## Frontend

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-035](decisions/ADR-035-frontend-architecture.md) | Frontend: Vite + React + TypeScript + Tailwind + feature-first | ✅ | 2026-07-15 |

## CI/CD & Containerização

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-036](decisions/ADR-036-containerization-strategy.md) | Dockerfiles multi-stage + docker compose + GitHub Actions | ✅ | 2026-07-15 |

## Observabilidade

| ADR | Título | Status | Data |
|-----|--------|--------|------|
| [ADR-037](decisions/ADR-037-observability.md) | Métricas (Micrometer/Prometheus) + Logging JSON + Tracing (OTel) | ✅ | 2026-07-16 |

---

## Resumo das decisões mais impactantes

| Decisão | Por que importa |
|---------|----------------|
| **Modular Monolith** (ADR-001) | Isolamento de domínio sem a complexidade operacional de microsserviços; pode ser decomposto se necessário |
| **BUG-1 pattern** (ADR-029) | Evita perda silenciosa de domain events após `save()` — bug que passa em todos os testes unitários mas quebra em integração |
| **@Async + AFTER_COMMIT** (ADR-011) | Garante que listeners cross-module nunca leem dados que ainda não foram commitados |
| **CompletableFuture sem @Transactional** (ADR-034) | Spring TX é ThreadLocal — `@Transactional` no DashboardService causaria silently noops nas threads filhas |
| **SSE via fetch() + ReadableStream** (ADR-022 + ADR-035 Frontend) | EventSource nativo não suporta Authorization header; fetch() é a única alternativa com JWT |
| **PK composta em audit_events** (ADR-032) | Particionamento por range exige que a coluna de partição faça parte da PK em PostgreSQL |
