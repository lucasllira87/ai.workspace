# ADR-034 — Parallel Dashboard Aggregation with CompletableFuture + Virtual Threads

**Status:** Accepted  
**Date:** 2026-07-15  
**Module:** Audit / Dashboard

---

## Context

`GET /api/v1/dashboard` aggregates stats from 4 sources: Documents, Learning, Billing, and recent Audit events. If executed sequentially and each DB query takes ~5 ms, total latency is ~20 ms. Under load or DB contention these can spike to 50–200 ms each, making sequential execution a 200–800 ms endpoint — unacceptable for a primary UI view.

## Decision

Use `CompletableFuture.supplyAsync()` to run the three independent stat fetches in parallel, then `CompletableFuture.allOf().join()` to synchronize before building the response.

```java
CompletableFuture<DocumentStats> docFuture =
        CompletableFuture.supplyAsync(() -> safeDocStats(userId), asyncExecutor);
CompletableFuture<LearningStats> learnFuture =
        CompletableFuture.supplyAsync(() -> safeLearnStats(userId), asyncExecutor);
CompletableFuture<BillingStats> billFuture =
        CompletableFuture.supplyAsync(() -> safeBillStats(userId), asyncExecutor);

CompletableFuture.allOf(docFuture, learnFuture, billFuture).join();

var recentActivity = auditEventRepository.findRecentByUserId(userId, 10);
```

The executor is `Executors.newVirtualThreadPerTaskExecutor()` — a Java 21 virtual thread executor registered as `"auditAsyncExecutor"` in `AuditConfig`.

## Transaction model

`DashboardService` carries **no class-level `@Transactional`**. Each `CompletableFuture` task runs on its own virtual thread with no inherited Spring transaction context (Spring binds transactions to `ThreadLocal` — a different thread means a different context). Each stat adapter delegates to Spring Data JPA repositories which are `@Transactional(readOnly = true)` by default — each query runs in its own short-lived transaction.

The `recentActivity` call runs on the HTTP handler thread after `allOf().join()`. It relies on `AuditEventJpaRepository`'s own transaction, not a wrapping service transaction.

This is intentional: attempting to propagate a single `@Transactional(readOnly=true)` transaction across three virtual threads would require `PROPAGATION_REQUIRES_NEW` on each adapter, yield no consistency benefit (we're only reading), and force all queries onto platform threads to avoid virtual-thread pinning under certain JDBC drivers.

## Virtual Threads over Platform Thread Pool

| Factor | Platform thread pool | Virtual threads |
|---|---|---|
| Sizing | Must tune pool size to handle concurrent requests | Self-scaling; OS schedules carrier threads |
| JDBC blocking | Blocks carrier thread during query; pool exhaustion under load | Virtual thread parks; carrier thread freed for other work |
| Configuration | `ThreadPoolTaskExecutor` with core/max/queue settings | `Executors.newVirtualThreadPerTaskExecutor()` — zero config |
| Java version | Any | Requires Java 21+ (GA since September 2023) |

Project targets Java 21 — virtual threads are the natural choice.

## Resilience

Each stat fetch is wrapped in a `safe*()` method that catches all exceptions, logs a warning, increments `audit.dashboard.fallbacks` counter, and returns an empty/unavailable fallback value. The dashboard always returns HTTP 200 even if one or more stat sources are down.

## Latency model

Expected latency: `max(doc_time, learning_time, billing_time) + audit_recent_time`

At p50 (~5 ms each): ~10 ms total.  
At p99 (~100 ms billing): ~105 ms total vs ~315 ms sequential.

## Alternatives Considered

| Option | Reason Rejected |
|---|---|
| Sequential stats fetching | Simple; unacceptable latency at p99 |
| WebFlux reactive pipeline | Full reactive stack required; existing code is MVC blocking |
| Materialized cache (Redis) | Best long-term solution; deferred to v1.1 |
| GraphQL with DataLoader | Appropriate for multi-field UI batching; over-engineered for a single endpoint |

## Roadmap

**v1.1 — Redis cache:** Cache `DashboardStats` per `userId` with 60s TTL (Caffeine L1 + Redis L2). Invalidate on Billing or Learning domain events. Eliminates DB load for frequent polling.
