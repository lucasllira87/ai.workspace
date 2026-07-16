# AI Workspace

**Plataforma SaaS de IA** construída do zero como exercício de engenharia de software de produção — cobrindo desde o domain model até CI/CD, observabilidade e auditoria de segurança.

> Document Assistant com RAG · Learning Platform · Billing com planos e cotas · Notificações em tempo real · Auditoria com particionamento por ano

---

## Por que este projeto existe

A maioria dos projetos de portfólio demonstra um recurso isolado. Este demonstra como **decisões arquiteturais interagem em escala real**: como domain events propagam efeitos entre módulos sem acoplamento direto, como um pipeline RAG multi-provider se comporta sob falha de provedor, como billing e AI coexistem sem criar dependências circulares.

O projeto passou por uma **auditoria técnica formal** (9 issues críticos encontrados e corrigidos, incluindo 4 migrations que impediam a aplicação de subir) antes de ser publicado. O relatório completo está em [`docs/backend/final-audit-report.md`](docs/backend/final-audit-report.md).

---

## Arquitetura

### Modular Monolith

Oito módulos com fronteiras explícitas, cada um seguindo Clean Architecture internamente. A comunicação entre módulos é feita exclusivamente via domain events — nenhum módulo importa classes de serviço de outro.

```
┌─────────────────────────────────────────────────────────────┐
│                        HTTP / SSE                           │
│              (Spring MVC + Virtual Threads)                 │
├──────────┬──────────┬──────────┬──────────┬────────────────┤
│  iam     │ billing  │documents │ learning │ notifications  │
│          │          │          │          │                │
│ domain   │ domain   │ domain   │ domain   │ domain         │
│ app      │ app      │ app      │ app      │ app            │
│ infra    │ infra    │ infra    │ infra    │ infra          │
├──────────┴──────────┴──────────┴──────────┴────────────────┤
│              aicore · audit · shared                        │
├─────────────────────────────────────────────────────────────┤
│     PostgreSQL + pgvector · Redis · Flyway migrations       │
└─────────────────────────────────────────────────────────────┘
```

### Fluxo de domain events

```
RegisterUserService
  └─ User.create()               ← domain event registrado no agregado
  └─ userRepository.save(user)
  └─ user.getDomainEvents()      ← publicado do objeto ORIGINAL (não do retorno do save)
       │
       ├─► BillingIamListener    @Async + @TransactionalEventListener(AFTER_COMMIT)
       │     └─ cria trial subscription
       └─► IamAuditListener      @Async + @TransactionalEventListener(AFTER_COMMIT)
             └─ registra evento de auditoria
```

O padrão `@Async + AFTER_COMMIT` garante que listeners leem dados já commitados e nunca bloqueiam o thread do request original.

### Pipeline RAG

```
Upload PDF/DOCX/MD
  └─ Extração de texto (Tika/PSPDFKit)
  └─ Chunking com overlap configurável
  └─ Embedding via provider (OpenAI / Anthropic / Google / Ollama)
       └─ FallbackChain + CircuitBreaker (Resilience4j)
  └─ Armazenamento em pgvector (HNSW, cosine similarity)

Chat com documento
  └─ Embed pergunta do usuário
  └─ similaritySearch (top-K com threshold mínimo)
  └─ Prompt com contexto delimitado (anti prompt-injection)
  └─ Streaming da resposta via SSE
```

---

## Decisões técnicas relevantes

| Decisão | Alternativa descartada | Motivo |
|---------|----------------------|--------|
| **Modular Monolith** | Microsserviços | Isolamento de domínio sem overhead operacional; decomponível quando necessário |
| **Domain events in-memory** | Mensageria externa | Suficiente para MVP; Transactional Outbox documentado como próximo passo em escala |
| **pgvector + HNSW** | Pinecone, Weaviate | Mantém tudo no PostgreSQL, sem infra adicional; HNSW cobre milhões de vetores |
| **Virtual Threads (Loom)** | Threads reativas (WebFlux) | SSE com blocking I/O sem mudança de paradigma; pool infinito sem overhead |
| **SSE via `fetch()` + ReadableStream** | `EventSource` nativo | `EventSource` não suporta `Authorization` header; `fetch()` permite JWT normalmente |
| **Flyway** | Hibernate DDL auto | Migrations versionadas, auditáveis e revertíveis; `validate` em prod |
| **Reconstitute vs. factory** | ORM direto | `Domain.create()` dispara eventos; `Domain.reconstitute()` não — evita re-publicação ao carregar do banco |

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
| Testes | JUnit 5 · Testcontainers · ArchUnit · JaCoCo ≥ 80% |

### Frontend

| Camada | Tecnologia |
|--------|-----------|
| Build | Vite 5 |
| UI | React 18 · TypeScript 5 (strict) |
| Estilo | Tailwind CSS 3 |
| Server state | TanStack Query v5 |
| Client state | Zustand (sessionStorage persist) |
| Formulários | React Hook Form · Zod |
| HTTP | Axios (JWT interceptor + refresh queue) |
| Streaming | `fetch()` + ReadableStream (SSE com Authorization) |

### Infraestrutura

| Componente | Tecnologia |
|-----------|-----------|
| Container | Docker · docker compose |
| CI | GitHub Actions (build + test + push paralelo) |
| Registry | GitHub Container Registry (ghcr.io) |
| Proxy | nginx (SPA fallback + SSE proxy) |
| Observabilidade | Prometheus · Grafana |

---

## Módulos

```
backend/src/main/java/com/aiworkspace/
├── iam/          Registro, login, refresh token, blacklist JWT, gerenciamento de usuários
├── billing/      Planos, assinaturas, cotas de tokens, trial 14 dias, webhooks de pagamento
├── documents/    Upload, extração de texto, chunking, embeddings, RAG, chat SSE
├── learning/     Cursos, aulas, matrículas, progresso, certificados
├── notifications/ E-mail (Mailgun/SMTP), notificações in-app, SSE push
├── audit/        Eventos de auditoria, particionamento anual, dashboard
├── aicore/       Orquestração de providers, circuit breaker, cost guard, usage metrics
└── shared/       AggregateRoot, DomainEvent, ValueObject, exceções, config
```

---

## Garantias arquiteturais (ArchUnit em CI)

Seis regras verificadas em todo PR:

1. Domain não importa nada de `infrastructure.*`
2. Application não importa nada de `infrastructure.*`
3. Módulos não importam entre si (exceto via domain events e shared)
4. Domain objects são POJO — sem `@Entity`, `@Service`, `@Component`
5. Controllers ficam em `presentation.*`
6. Adapters JPA ficam em `persistence.adapter.*`

---

## Observabilidade

Todo request carrega no MDC: `requestId · userId · traceId · spanId · httpMethod · httpPath`

- **Métricas**: `/actuator/prometheus` → Prometheus → Grafana
- **Logs**: JSON estruturado em prod (um objeto por linha, compatível com ELK/Loki)
- **Tracing**: Micrometer Tracing + OTel bridge → OTLP collector (Jaeger/Tempo)

---

## CI/CD

| Workflow | Trigger | Jobs |
|---------|---------|------|
| `ci.yml` | Push em qualquer branch · PR para main | `backend-ci` (mvn verify) + `frontend-ci` (type-check + lint + build) em paralelo |
| `release.yml` | Push em `main` | CI → build + push de imagens para `ghcr.io` com tags `sha` e `latest` |

---

## Documentação

- [Como rodar o sistema](RUNNING.md)
- [Guia de desenvolvimento local](docs/setup/local-dev.md)
- [Visão geral da arquitetura](docs/architecture/overview.md)
- [Índice de ADRs (37 decisões documentadas)](docs/architecture/adr-index.md)
- [Relatório de auditoria final](docs/backend/final-audit-report.md)
