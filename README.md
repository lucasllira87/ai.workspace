# AI Workspace

Plataforma SaaS de IA construída como **Modular Monolith** com **Clean Architecture + DDD** por módulo.

> Document Assistant com RAG · Learning Platform · Billing com planos e cotas · Notificações em tempo real · Auditoria com particionamento por ano

---

## Stack

### Backend
| Camada | Tecnologia |
|--------|-----------|
| Framework | Spring Boot 3.3 · Java 21 |
| Concorrência | Virtual Threads (Project Loom) |
| Banco de dados | PostgreSQL 16 + pgvector |
| Cache / blacklist | Redis 7 |
| Migrações | Flyway |
| Segurança | Spring Security · JJWT 0.12 |
| Métricas | Micrometer · Prometheus |
| Tracing | Micrometer Tracing · OpenTelemetry OTLP |
| Logging | Logback · logstash-logback-encoder (JSON em prod) |
| AI providers | OpenAI · Anthropic · Google AI · Ollama |
| Resiliência | Resilience4j (circuit breaker + retry) |
| Documentação API | Springdoc / OpenAPI 3 |
| Testes | JUnit 5 · Testcontainers · ArchUnit · JaCoCo ≥ 80% |

### Frontend
| Camada | Tecnologia |
|--------|-----------|
| Build | Vite 5 |
| UI | React 18 · TypeScript 5 (strict) |
| Estilo | Tailwind CSS 3 |
| Roteamento | React Router v6 |
| Server state | TanStack Query v5 |
| Client state | Zustand (sessionStorage persist) |
| Formulários | React Hook Form · Zod |
| HTTP | Axios (JWT interceptor + refresh queue) |
| Streaming | `fetch()` + ReadableStream (SSE com Authorization header) |

### Infraestrutura
| Componente | Tecnologia |
|-----------|-----------|
| Container | Docker · docker compose |
| CI | GitHub Actions |
| Registry | GitHub Container Registry (ghcr.io) |
| Proxy | nginx (SPA fallback + SSE proxy) |
| Observabilidade | Prometheus · Grafana |

---

## Início rápido

### Pré-requisitos
- Docker 24+ e Docker Compose v2
- Java 21+ (para rodar o backend no host)
- Node 20+ (para rodar o frontend no host)

### 1. Configurar variáveis de ambiente

```bash
cp .env.example .env
# Edite .env e defina pelo menos:
# POSTGRES_PASSWORD, REDIS_PASSWORD, JWT_SECRET
```

### 2. Subir infraestrutura (Postgres + Redis)

```bash
docker compose up -d
```

### 3. Rodar o backend

```bash
cd backend
./mvnw spring-boot:run
# API disponível em http://localhost:8080
# Swagger UI em http://localhost:8080/swagger-ui.html
```

### 4. Rodar o frontend

```bash
cd frontend
npm install
npm run dev
# App disponível em http://localhost:5173
```

### Stack completa em Docker

```bash
docker compose --profile full up --build
# Backend: http://localhost:8080
# Frontend: http://localhost:3000
```

### Com observabilidade (Prometheus + Grafana)

```bash
docker compose -f docker-compose.yml \
               -f docker-compose.observability.yml \
               --profile full up --build
# Prometheus: http://localhost:9090
# Grafana:    http://localhost:3001  (admin/admin)
```

---

## Estrutura do repositório

```
ai-workspace/
├── backend/                        # Spring Boot — Modular Monolith
│   ├── src/main/java/com/aiworkspace/
│   │   ├── AiWorkspaceApplication.java
│   │   ├── iam/                    # Módulo: autenticação e usuários
│   │   ├── billing/                # Módulo: planos, assinaturas, cotas
│   │   ├── documents/              # Módulo: upload, indexação, RAG, chat
│   │   ├── learning/               # Módulo: cursos, matrículas, progresso
│   │   ├── notifications/          # Módulo: e-mail e notificações in-app
│   │   ├── audit/                  # Módulo: eventos de auditoria + dashboard
│   │   └── shared/                 # DomainEvent, ValueObject, exceções, config
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── logback-spring.xml
│   │   └── db/migration/           # Scripts Flyway
│   └── src/test/
│       └── java/com/aiworkspace/
│           ├── architecture/        # ArchUnit — 6 regras arquiteturais
│           └── shared/testcontainers/
├── frontend/                       # React — feature-first
│   └── src/
│       ├── app/                    # App, router, providers
│       ├── features/               # auth · dashboard · documents · billing · notifications
│       ├── shared/                 # api (axios), store (Zustand), components, ui
│       └── pages/                  # NotFoundPage
├── observability/
│   ├── prometheus.yml
│   └── grafana/provisioning/
├── docs/
│   ├── architecture/
│   │   ├── overview.md
│   │   ├── adr-index.md
│   │   └── decisions/              # ADR-001 … ADR-037
│   ├── backend/                    # Revisões formais por fase
│   └── setup/
│       └── local-dev.md
├── docker-compose.yml
├── docker-compose.override.yml     # Dev: só infra
├── docker-compose.observability.yml
└── .env.example
```

---

## Módulos do backend

Cada módulo segue a mesma estrutura de 4 camadas:

```
<módulo>/
├── domain/          # Entidades, agregados, value objects, domain events, ports (interfaces)
├── application/     # Use cases / services — orquestram domain, publicam eventos
├── infrastructure/  # Adapters JPA, adapters de porta, configs, schedulers
└── presentation/    # Controllers REST, DTOs de entrada/saída
```

| Módulo | Responsabilidade principal |
|--------|---------------------------|
| `iam` | Registro, login, refresh token, gerenciamento de usuários |
| `billing` | Planos, assinaturas, cotas de uso, trial |
| `documents` | Upload, extração de texto, chunking, embeddings, RAG, chat SSE |
| `learning` | Cursos, aulas, matrículas, progresso, certificados |
| `notifications` | Envio de e-mail (Mailgun/SMTP), notificações in-app, SSE push |
| `audit` | Gravação de eventos de auditoria, particionamento anual, dashboard |

---

## Testes

```bash
# Testes unitários (rápidos, sem Docker)
cd backend && ./mvnw test

# Testes unitários + integração (Testcontainers — requer Docker)
cd backend && ./mvnw verify

# Relatório de cobertura (JaCoCo — threshold 80%)
# macOS: open backend/target/site/jacoco/index.html
# Linux:  xdg-open backend/target/site/jacoco/index.html
# Windows: start backend/target/site/jacoco/index.html

# Type-check + lint + build do frontend
cd frontend && npm run type-check && npm run lint && npm run build
```

---

## CI/CD

| Workflow | Trigger | Jobs |
|---------|---------|------|
| `ci.yml` | Push em qualquer branch · PR para main | `backend-ci` (mvn verify) · `frontend-ci` (type-check + lint + build) em paralelo |
| `release.yml` | Push em `main` | Chama CI → build + push de imagens para `ghcr.io` com tags `sha` e `latest` |

---

## Observabilidade

Todo request carrega no MDC: `requestId` · `userId` · `traceId` · `spanId` · `httpMethod` · `httpPath`

- **Métricas**: `/actuator/prometheus` → Prometheus → Grafana
- **Logs**: JSON estruturado em prod (um objeto JSON por linha)
- **Tracing**: Micrometer Tracing + OTel bridge → OTLP collector

---

## Documentação

- [Guia de desenvolvimento local](docs/setup/local-dev.md)
- [Visão geral da arquitetura](docs/architecture/overview.md)
- [Índice de ADRs](docs/architecture/adr-index.md)
- [Swagger UI](http://localhost:8080/swagger-ui.html) (com backend rodando)
