# Visão Geral da Arquitetura — AI Workspace

## Estilo arquitetural

**Modular Monolith** com **Clean Architecture + DDD** por módulo.

O sistema é deployado como um único processo Spring Boot, mas internamente organizado em módulos com fronteiras rígidas — cada módulo tem seu próprio pacote raiz, seu próprio schema de banco (via Flyway), e se comunica com outros módulos exclusivamente via **Domain Events assíncronos** ou **leituras JPA diretas somente-leitura** (documentado em ADR-033).

```
┌─────────────────────────────────────────────────────────┐
│                    ai-workspace (JVM)                    │
│                                                          │
│  ┌─────────┐  ┌─────────┐  ┌───────────┐  ┌─────────┐  │
│  │  auth   │  │ billing │  │ documents │  │learning │  │
│  └────┬────┘  └────┬────┘  └─────┬─────┘  └────┬────┘  │
│       │             │             │              │        │
│  ┌────┴─────────────┴─────────────┴──────────────┴────┐  │
│  │              Spring Application Event Bus           │  │
│  │         (@TransactionalEventListener + @Async)      │  │
│  └────────────────────────┬────────────────────────────┘  │
│                           │                               │
│  ┌─────────────┐  ┌───────┴──────┐                        │
│  │notifications│  │    audit     │                        │
│  └─────────────┘  └──────────────┘                        │
└─────────────────────────────────────────────────────────┘
         │                    │
    PostgreSQL 16         Redis 7
    + pgvector
```

---

## Módulos

### `auth`
Responsável por: registro de usuários, autenticação (login), geração e validação de JWT (access + refresh), blacklist de tokens no Redis.

Publica: `UserRegisteredEvent`, `UserBlockedEvent`

### `billing`
Responsável por: planos (`Plan`), assinaturas (`Subscription`), ciclo de vida do trial, cotas de uso (`UsageLedger`), renovação automática.

Ouve: `UserRegisteredEvent` → cria trial automaticamente
Publica: `SubscriptionActivatedEvent`, `SubscriptionCanceledEvent`, `TrialExpiringEvent`

### `documents`
Responsável por: upload de arquivos, extração de texto (PDF/DOCX/HTML/MD), chunking, geração de embeddings via AI provider, busca vetorial (pgvector), chat RAG com streaming SSE.

Ouve: `DocumentUploadedEvent` → inicia pipeline de indexação assíncrona
Publica: `DocumentIndexedEvent`, `DocumentDeletedEvent`

### `learning`
Responsável por: cursos, aulas, matrículas (`Enrollment`), progresso de lições, emissão de certificado ao completar curso.

Ouve: `SubscriptionCanceledEvent` → pode suspender acesso a cursos pagos
Publica: `CourseCompletedEvent`, `LessonCompletedEvent`

### `notifications`
Responsável por: envio de e-mail via SMTP (Mailgun), notificações in-app, entrega via SSE, preferências de notificação por usuário.

Ouve: múltiplos eventos de todos os módulos
Não publica eventos de domínio

### `audit`
Responsável por: gravação de todos os `AuditEvent` com particionamento anual no PostgreSQL, manutenção automática de partições (`PartitionMaintenanceScheduler`), dados para o `DashboardService`.

Ouve: todos os eventos relevantes (via listeners assíncronos)
Expõe: `DashboardService` com stats paralelas via `CompletableFuture`

### `shared`
Código sem lógica de negócio compartilhado entre módulos: `AggregateRoot`, `DomainEvent`, `ValueObject`, exceções comuns, `GlobalExceptionHandler`, `ObservabilityConfig`, OpenAPI config.

---

## Camadas por módulo (Clean Architecture)

```
presentation/   ← Controllers REST, DTOs de entrada/saída, validação HTTP
    │
application/    ← Use cases / Services: orquestram domain, publicam eventos
    │
domain/         ← Entidades, agregados, value objects, domain events, ports (interfaces)
    │
infrastructure/ ← Adapters JPA (implementam ports), configs, schedulers, adapters externos
```

**Regra de dependência:** as setas apontam para dentro. `domain` não importa nada das camadas externas.

---

## Padrões transversais

### Domain Events — publicação correta (BUG-1 pattern)

```java
// CORRETO: publicar a partir do objeto original, não do retorno de save()
User saved = userRepository.save(user);
user.getDomainEvents().forEach(eventPublisher::publishEvent);
user.clearDomainEvents();
```

O `save()` do Spring Data retorna um objeto reconstituted — os domain events do agregado original seriam perdidos se publicados a partir do objeto retornado.

### @Async + @TransactionalEventListener(AFTER_COMMIT)

**Todos** os listeners cross-module são assíncronos e aguardam o commit da transação originadora:

```java
@Async("auditAsyncExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void on(UserRegisteredEvent event) { ... }
```

Isso garante que o evento só é processado após o dado estar visível no banco. ArchUnit verifica essa regra em tempo de build (Rule 4).

### Dashboard — CompletableFuture paralelo

`DashboardService` **não tem `@Transactional`** — o contexto de transação do Spring é ThreadLocal e não propaga para as threads de CompletableFuture. Cada chamada JPA dentro de `safeDocStats()`, `safeLearnStats()` e `safeBillStats()` gerencia sua própria transação curta (Spring Data default).

```java
CompletableFuture<DocumentStats> docFuture =
    CompletableFuture.supplyAsync(() -> safeDocStats(userId), asyncExecutor);
// ... demais futures em paralelo ...
CompletableFuture.allOf(docFuture, learnFuture, billFuture).join();
```

### Particionamento do `audit_events`

A tabela `audit.audit_events` usa **range partitioning por ano** no PostgreSQL. A PK composta é `(id UUID, occurred_at TIMESTAMPTZ)` com `@IdClass(AuditEventId.class)`. O `PartitionMaintenanceScheduler` cria a partição do próximo ano mensalmente (`@Scheduled(cron = "0 0 3 1 * *")`).

### Cross-module reads (leituras JPA diretas)

Para montar o `DashboardStats`, o módulo `audit` acessa diretamente os repositórios JPA de `documents`, `learning` e `billing` (leitura somente). Documentado em ADR-033 como exceção deliberada — evita a latência de eventos e a complexidade de projeções CQRS para uma feature de dashboard.

---

## Fluxos principais

### Registro de usuário + criação de trial

```
POST /api/auth/register
    → RegisterUserService.register()
        → User.create() → domain event: UserRegisteredEvent
        → userRepository.save(user)
        → eventPublisher.publishEvent(UserRegisteredEvent)  ← após commit

    [async, AFTER_COMMIT]
    → SubscriptionService (billing) cria Subscription em TRIAL
    → AuditService grava AuditEvent{module=AUTH, type=USER_REGISTERED}
    → NotificationService envia e-mail de boas-vindas
```

### Upload + indexação de documento

```
POST /api/documents  (multipart)
    → DocumentService.upload()
        → Document.upload() → state: UPLOADED → event: DocumentUploadedEvent
        → storageAdapter.store(file)
        → documentRepository.save(document)
        → eventPublisher.publishEvent(DocumentUploadedEvent)

    [async, AFTER_COMMIT]
    → DocumentIndexingService
        → Document.startIndexing() → state: INDEXING
        → textExtractor.extract() → chunker.chunk()
        → embeddingPort.embed(chunks) → vectorRepository.saveAll()
        → Document.markAsIndexed() → event: DocumentIndexedEvent
    → AuditService grava evento
```

### Chat com documento (SSE streaming)

```
POST /api/documents/{id}/chat
    → ChatService.chat(documentId, question)
        → usage quota check (billingPort)
        → vectorRepository.findSimilar(questionEmbedding, topK=5)
        → aiProvider.streamChat(context + question)
            → response.getBody() → ReadableStream → SSE chunks → cliente
        → usage ledger update
```

### Dashboard (paralelo)

```
GET /api/dashboard
    → DashboardService.getStats(userId)
        → CompletableFuture.allOf(
               supplyAsync(safeDocStats),    ← virtual thread 1
               supplyAsync(safeLearnStats),  ← virtual thread 2
               supplyAsync(safeBillStats)    ← virtual thread 3
           ).join()
        → auditEventRepository.findRecentByUserId()
        → DashboardStats.of(docStats, learnStats, billStats, recentActivity)
```

---

## Observabilidade — cadeia MDC

```
Request HTTP
    │
    ▼ MdcRequestFilter (HIGHEST_PRECEDENCE+10, antes do Security)
    │   MDC: requestId, httpMethod, httpPath
    │
    ▼ Spring Security (JWT filter)
    │   SecurityContextHolder ← autenticado
    │
    ▼ MdcUserInterceptor (HandlerInterceptor, dentro do DispatcherServlet)
    │   MDC: userId
    │
    ▼ Micrometer Tracing (automático, OTel bridge)
    │   MDC: traceId, spanId
    │
    ▼ Controller → Service → Repository
        Todos os logs têm: requestId · userId · traceId · spanId
```

---

## Frontend — estrutura feature-first

```
src/features/<nome>/
├── api.ts          ← Axios calls + Zod parse (validação na borda HTTP)
├── types.ts        ← tipos locais ou re-exports de shared/api/types
├── hooks/          ← TanStack Query mutations/queries
├── components/     ← componentes da feature
└── pages/          ← páginas roteadas
```

Estado de autenticação: Zustand com `sessionStorage` persist (tokens expiram ao fechar a aba).
Estado de servidor: TanStack Query (sem Redux).
SSE: `fetch()` + `ReadableStream` com `Authorization` header (EventSource não suporta header customizado).

---

## Regras arquiteturais (ArchUnit)

6 regras verificadas em tempo de build (`mvn verify`):

| Regra | Descrição |
|-------|-----------|
| 1 | `domain/` não importa `infrastructure/` nem `presentation/` |
| 2 | `application/` não importa `presentation/` |
| 3 | Controllers vivem apenas em `presentation/` |
| 4 | **Todo `@TransactionalEventListener` deve ser `@Async`** |
| 5 | Domain events implementam `DomainEvent` |
| 6 | Adapters em `infrastructure/persistence/adapter/` implementam ao menos uma interface |

---

## Decisões de arquitetura

Veja o [índice completo de ADRs](adr-index.md) para todas as 37 decisões arquiteturais registradas.
