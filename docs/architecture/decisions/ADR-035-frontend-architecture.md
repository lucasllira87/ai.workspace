# ADR-035: Frontend Architecture — Vite + React + Feature-First

**Status:** Accepted  
**Date:** 2026-07-15  
**Phase:** 7 — Frontend

---

## Context

The SaaS AI Workspace platform requires a frontend that integrates with the modular monolith backend over REST + SSE. Key constraints:

- JWT authentication with silent token refresh (no logout on 401)
- Real-time notification delivery from backend without polling overhead
- AI document chat with streaming responses
- Strict separation of concerns to mirror the backend's modular structure

---

## Decision

### Stack

| Concern | Library | Version |
|---------|---------|---------|
| Build | Vite | 5 |
| UI framework | React | 18 |
| Type system | TypeScript strict | 5 |
| Styling | Tailwind CSS | 3 + tailwindcss-animate |
| Routing | React Router | v6 (Outlet pattern) |
| Server state | TanStack Query | v5 |
| Client state | Zustand | 4 |
| Schema validation | Zod | 3 |
| Forms | React Hook Form + @hookform/resolvers | 7 |
| HTTP client | Axios | 1 |
| Icons | Lucide React | latest |
| Date formatting | date-fns | 3 |

### Folder structure — Feature-first

```
src/
├── app/           # App, router, providers (entry composition)
├── features/
│   ├── auth/      # api, components, hooks, pages, types
│   ├── dashboard/ # api, components, pages
│   ├── documents/ # api, components, pages (includes ChatPanel)
│   ├── billing/   # api, components, pages
│   └── notifications/ # api, hooks (SSE), components, types
├── shared/
│   ├── api/       # axios instance + types.ts (Zod schemas)
│   ├── components/# ui/, Layout/, RequireAuth
│   └── store/     # authStore (Zustand)
└── pages/         # NotFoundPage
```

### JWT interceptor with refresh queue (`shared/api/axios.ts`)

- Request interceptor: reads `useAuthStore.getState().accessToken` (outside React — correct for module-level singletons)
- Response interceptor: on 401, checks `_retry` flag to avoid infinite loops; uses `isRefreshing` boolean + `refreshQueue` array to serialize concurrent refresh attempts
- On refresh failure: drains queue with rejection + calls `logout()`

### Session persistence (Zustand + sessionStorage)

Tokens are persisted to `sessionStorage` (not `localStorage`) using `zustand/middleware/persist` + `partialize`. This means:
- Tokens survive page reloads within the same browser tab
- Tokens are cleared when the tab is closed
- Multiple tabs do NOT share the same session (security isolation)

### SSE via `fetch()` + `ReadableStream`

Native `EventSource` does not support custom headers, making JWT attachment impossible. Instead:

```ts
const response = await fetch('/api/notifications/stream', {
  headers: { Authorization: `Bearer ${accessToken}` },
  signal: controller.signal,
})
const reader = response.body.getReader()
```

Buffer accumulation handles SSE lines split across TCP packets. Reconnect in 5s on non-abort errors. `active` flag + `AbortController` ensures cleanup when token changes or component unmounts.

### Zod validation at HTTP boundary

All `api.ts` files parse HTTP responses through Zod schemas defined in `shared/api/types.ts`. This catches backend contract drift at the point of ingestion, not deep in components.

---

## Consequences

**Positive:**
- Feature isolation mirrors backend module isolation — changes to one feature don't cascade
- JWT refresh queue eliminates thundering-herd logout on token expiry under concurrent requests
- SSE with `fetch()` allows Authorization header and is more controllable than `EventSource`
- Zod boundary validation provides runtime safety against API changes

**Negative:**
- `Header.tsx` (shared layout) imports `NotificationBell` from `features/notifications/` — creates a `shared → feature` dependency. Accepted tradeoff: injecting it via prop or context adds ceremony for a single stable dependency
- `tailwindcss-animate` required as devDependency for Toast entry animations

---

## Alternatives Rejected

| Alternative | Reason |
|------------|---------|
| Redux Toolkit | Overkill for auth-only global state; TanStack Query handles server state |
| `localStorage` for tokens | Survives tab close — increases XSS token exposure window |
| Native `EventSource` for SSE | Cannot set `Authorization` header |
| Module-based folder structure | Feature-first keeps related code co-located; easier to delete a feature |
