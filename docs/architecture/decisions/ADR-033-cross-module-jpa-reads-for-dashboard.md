# ADR-033 — Cross-Module JPA Reads for Dashboard Stats

**Status:** Accepted  
**Date:** 2026-07-15  
**Module:** Audit / Dashboard

---

## Context

The Dashboard module needs aggregated stats from three peer modules:

| Stat group | Source module | Data needed |
|---|---|---|
| Document stats | Documents | count(total), count(indexed), sum(sizeBytes) per user |
| Learning stats | Learning | count(enrollments), count(completed), count(certificates) per user |
| Billing stats | Billing | active subscription, plan, token quota, tokens used this period |

These are **read-only aggregations**, not business operations. The stats do not require domain logic from the source modules — they are counts and sums over persisted state.

## Decision

Dashboard stats adapters (`DocumentStatsAdapter`, `LearningStatsAdapter`, `BillingStatsAdapter`) **directly import and inject the other modules' Spring Data JPA repositories**.

```java
// In the audit module's infrastructure layer
@Component
public class DocumentStatsAdapter implements DocumentStatsPort {
    private final DocumentJpaRepository documentJpaRepository; // from documents module
    ...
}
```

New derived queries are added to the source modules' `JpaRepository` interfaces:
- `DocumentJpaRepository`: `countByOwnerId`, `countByOwnerIdAndStatus`, `sumSizeBytesByOwnerId`
- `EnrollmentJpaRepository`: `countByUserId`, `countByUserIdAndStatus`
- `CertificateJpaRepository`: `countByUserId`

## Tradeoffs

**Coupling introduced:**
- The audit module is aware of `documents`, `learning`, and `billing` JPA entity schemas.
- Renaming a field (e.g., `status` → `indexingStatus`) in a source module breaks `DocumentStatsAdapter` at compile time (safe failure).
- Renaming a String value (`"INDEXED"` → `"indexed"`) breaks at runtime only — a latent coupling risk.

**Alternatives considered:**

| Option | Assessment |
|---|---|
| Each source module exposes a `StatsPort` the audit module calls | Clean — zero coupling to JPA schema; source module owns the query | Verbose: adds a Port, an adapter, and a config registration per module (12 classes for 3 modules) |
| Domain events carry stats deltas (event-sourced counters) | Scalable and fully decoupled; requires materialized views or in-memory state; enormous complexity for MVP |
| REST calls between modules | Wrong layer for a monolith; adds latency, fake network boundary |

**Why pragmatic coupling is acceptable here:**

1. All access is read-only — no risk of corrupting domain state.
2. The queries are derived JPA methods (`countBy*`) or standard JPQL — no native SQL that bypasses ORM.
3. At module extraction to microservices, `DocumentStatsAdapter` becomes a REST/gRPC call — the Port interface (`DocumentStatsPort`) is already the seam. The adapter is the only class that changes.
4. Scale: 3 modules × 1 adapter × ~3 queries = 9 queries total. Full Port/Adapter abstraction would add ~12 classes for no functional gain in the monolith phase.

## Constraints

- Adapters MUST only call `JpaRepository` read methods — never `save`, `delete`, or mutation methods from another module's repository.
- String-typed status comparisons (e.g., `"INDEXED"`, `"COMPLETED"`) must use constants — extracted from source module entity packages or duplicated with a comment linking to the source.
- ADR must be revisited when extracting any of the three source modules to a separate service.

## Consequences

- Dashboard loads 4–7 DB queries per request (3 stat adapters × 1–3 queries + 1 recent audit query).
- `DashboardService` parallelizes the 3 stat fetches via `CompletableFuture` + virtual threads to bound latency to `max(doc, learning, billing)` rather than their sum.
- Each safe* fallback ensures partial failures degrade gracefully — dashboard always returns a response, even if one stats source is unavailable.
