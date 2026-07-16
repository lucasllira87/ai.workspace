# ADR-037: Observability Strategy — Metrics, Structured Logging, Distributed Tracing

**Status:** Accepted  
**Date:** 2026-07-15  
**Phase:** 9 — Observabilidade

---

## Context

The AI Workspace platform runs as a modular monolith with async event processing (virtual threads), parallel CompletableFuture operations in DashboardService, and AI provider integrations that can fail or degrade. Debugging production issues requires correlated logs, metrics, and traces across these async boundaries.

---

## Decision

### Three pillars

| Pillar | Library | Transport |
|--------|---------|-----------|
| Metrics | Micrometer + `micrometer-registry-prometheus` | `/actuator/prometheus` scraped by Prometheus |
| Structured logging | Logback + `logstash-logback-encoder 7.4` | JSON to stdout → log aggregator |
| Distributed tracing | `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp` | OTLP/HTTP to collector |

### MDC population strategy

Two separate components populate MDC at different points in the request lifecycle:

```
HTTP request arrives
    │
    ▼
MdcRequestFilter (order HIGHEST_PRECEDENCE+10)
    │  Writes: requestId, httpMethod, httpPath
    │  Echoes: X-Request-ID response header
    ▼
Spring Security filters
    │  JWT validated → SecurityContextHolder populated
    ▼
DispatcherServlet
    │
    ▼
MdcUserInterceptor.preHandle()
    │  Writes: userId (from SecurityContextHolder)
    ▼
Controller → Service → Repository
    (all log calls have full MDC context)
    │
    ▼
MdcUserInterceptor.afterCompletion() → MDC.remove("userId")
    │
    ▼
MdcRequestFilter finally block → MDC.clear()
```

`MdcRequestFilter` must run before Spring Security so auth failures carry `requestId`. `MdcUserInterceptor` must run after JWT auth so `userId` is available. These constraints require two separate interception points.

Micrometer Tracing automatically populates `traceId` and `spanId` in MDC when `micrometer-tracing-bridge-otel` is on the classpath.

### Logback profiles

- **Non-prod**: `PatternLayout` with color highlighting, traceId/spanId/requestId/userId inline
- **Prod**: `LogstashEncoder` outputting one JSON object per line. Fields: `timestamp`, `level`, `logger_name`, `message`, `traceId`, `spanId`, `requestId`, `userId`, `httpMethod`, `httpPath`, plus stack traces via `ShortenedThrowableConverter`

### Common metric tags

`MeterRegistryCustomizer` applies `application` and `environment` tags to every metric. This enables:

```promql
# Filter to just this app in a shared Prometheus
rate(http_server_requests_seconds_count{application="ai-workspace"}[5m])

# Compare environments
http_server_requests_seconds_p99{application="ai-workspace", environment="prod"}
```

### `@Timed` and `@Observed`

`spring-boot-starter-aop` unlocks two AOP-based instrumentation annotations:
- `@Timed` — creates a `Timer` metric for the annotated method (via `TimedAspect`)
- `@Observed` — creates both a metric AND a span (via `ObservedAspect`, auto-configured)

Existing hand-crafted `Counter` and `Timer` in `AuditService` and `DashboardService` continue to work unchanged.

### Infrastructure overlay

`docker-compose.observability.yml` is an optional overlay (not merged into the base compose) to avoid polluting the dev environment. Prometheus scrapes `/actuator/prometheus` every 10s. Grafana auto-provisions Prometheus as the default datasource.

### Tracing sampling

`management.tracing.sampling.probability: 1.0` (100%) is appropriate for dev/staging. For production, reduce to `0.1` (10%) or use head-based sampling via the OTel collector.

---

## Consequences

**Positive:**
- Every log line in prod carries `traceId` — one grep correlates all logs for a request
- `requestId` (from `X-Request-ID` header or generated) allows external callers to correlate
- Zero-change to existing business code — MDC is populated transparently
- `@Timed` and `@Observed` available for fine-grained instrumentation without boilerplate

**Negative/Tradeoffs:**
- `MDC.clear()` in `MdcRequestFilter.finally` clears `traceId`/`spanId` that Micrometer Tracing set later in the chain. Micrometer Tracing re-populates MDC for each span, so this is safe — they are set before any log call in the chain and cleared after span completion
- OTLP exporter connects to `localhost:4318` in dev by default — connection refused logs appear if no collector is running. Suppressed at WARN level via logback config
- 100% trace sampling in prod is not suitable above ~1k req/s — must reduce before going to production

---

## Alternatives Rejected

| Alternative | Reason |
|------------|--------|
| Spring Cloud Sleuth | Deprecated in Spring Boot 3; Micrometer Tracing is the official replacement |
| Log4j2 JSON layout | Logstash encoder has richer MDC integration and `StructuredArguments` API |
| Single `MdcFilter` for both requestId and userId | userId only exists after JWT auth, which happens after `HIGHEST_PRECEDENCE+10` filter — cannot read SecurityContext at that point |
| `Zipkin` exporter | OTLP is vendor-neutral and works with Jaeger, Grafana Tempo, Datadog, etc. |
