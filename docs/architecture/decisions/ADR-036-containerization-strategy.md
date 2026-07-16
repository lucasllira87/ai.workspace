# ADR-036: Containerization & CI/CD Strategy

**Status:** Accepted  
**Date:** 2026-07-15  
**Phase:** 8 — CI/CD & Containerização

---

## Context

The platform requires a reproducible build and deployment pipeline that:
- Produces minimal, secure Docker images for backend and frontend
- Supports local development with only infrastructure in Docker
- Enforces quality gates (tests, type-check, lint) before any image is pushed
- Publishes immutable, traceable images to a container registry

---

## Decision

### Multi-stage Dockerfiles

**Backend:** `maven:3.9-eclipse-temurin-21` (build) → `eclipse-temurin:21-jre-alpine` (runtime)
- `pom.xml` copied before `src/` to maximize Docker layer cache on dep changes
- `mvn dependency:go-offline` in a separate layer
- JRE-only runtime (no JDK, no Maven) — ~200MB smaller than JDK image
- Non-root user `app` created and applied before EXPOSE/ENTRYPOINT
- ZGC Generational GC for sub-millisecond pauses: `-XX:+UseZGC -XX:+ZGenerational`
- `-XX:MaxRAMPercentage=75.0` for cgroup-aware heap sizing in containers

**Frontend:** `node:20-alpine` (build) → `nginx:1.27-alpine` (runtime)
- `package*.json` copied before source for npm cache layer
- `nginx:alpine` master process runs as root (required for port 80 bind); workers run as `nginx` user
- Alternative: `nginxinc/nginx-unprivileged` on port 8080 — deferred, not required for current scale

### nginx.conf design decisions

1. **SSE support**: `proxy_buffering off`, `proxy_cache off`, `proxy_read_timeout 3600s`, `proxy_http_version 1.1` — chunks must flow through immediately for Server-Sent Events
2. **Security headers in every location**: nginx `add_header` does NOT inherit from parent blocks when a child location has its own `add_header`. Security headers (`X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`) are repeated in all locations that declare `add_header`.
3. **Static asset caching**: `Cache-Control: public, immutable, max-age=31536000` — safe because Vite content-hashes all asset filenames
4. **Health endpoint** `/healthz`: returns 200 instantly; used by Docker HEALTHCHECK and future load balancer probes

### docker-compose health chain

Full dependency chain with `condition: service_healthy`:

```
db (pg_isready) → backend (actuator/health, start_period 30s) → frontend
redis (redis-cli ping) ↗
```

`start_period: 30s` on backend prevents false negatives during Spring Boot startup (Flyway migrations + bean wiring).

### Dev override pattern

`docker-compose.override.yml` assigns `profiles: ["full"]` to backend and frontend:
- `docker compose up` → only `db` + `redis` (dev mode)
- `docker compose --profile full up` → complete stack
- No separate `docker-compose.dev.yml` file needed

### CI/CD pipeline structure

**`ci.yml`**: Runs on every push and every PR to main
- `backend-ci` and `frontend-ci` jobs run in **parallel**
- Maven local repo cached via `setup-java: cache: maven`
- npm cache keyed on `frontend/package-lock.json`
- `concurrency: cancel-in-progress: true` — stale runs cancelled automatically
- JaCoCo report uploaded even on failure (`if: always()`)

**`release.yml`**: Runs only on push to main, calls `ci.yml` as a reusable workflow (`needs: ci`)
- Login to GHCR via `GITHUB_TOKEN` — no external secrets
- BuildKit layer cache stored in GitHub Actions cache (`type=gha`) with per-service scopes
- Images tagged with both `github.sha` (immutable, for rollback) and `latest`

---

## Consequences

**Positive:**
- Backend image: ~200MB (JRE Alpine) vs ~600MB (JDK full)
- Layer cache means a source-only change doesn't re-download all Maven deps
- Full healthcheck chain prevents cascading startup failures
- CI blocks image push on any test/lint/type-check failure
- `GITHUB_TOKEN` means zero secrets management for container registry auth

**Negative/Tradeoffs:**
- nginx runs master as root (port 80 binding constraint)
- `actuator/health` endpoint must be exposed and reachable inside the container (requires `spring-boot-starter-actuator` in pom.xml)
- Security headers repeated in 3 location blocks (nginx inheritance limitation — no cleaner alternative without `headers_more` nginx module)
