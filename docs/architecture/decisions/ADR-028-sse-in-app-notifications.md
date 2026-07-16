# ADR-028: SSE para notificações IN_APP em tempo real

**Status:** Accepted  
**Date:** 2026-07-15  
**Module:** notifications

---

## Contexto

O módulo de Notifications precisa entregar notificações IN_APP ao browser do usuário sem que o cliente precise fazer polling. As opções são: polling HTTP, WebSocket e Server-Sent Events (SSE).

## Decisão

Utilizar SSE via `SseEmitter` do Spring MVC para o endpoint `GET /api/v1/notifications/stream`.

## Alternativas avaliadas

| Opção | Prós | Contras |
|---|---|---|
| HTTP Polling | Simples | Latência, desperdício de recursos, 60 req/min/usuário |
| WebSocket | Bidirecional, baixa latência | Complexidade, não funciona via alguns proxies HTTP/1.1 |
| SSE | Unidirecional, auto-reconexão, HTTP padrão | Thread por conexão (Tomcat blocking) |
| SSE + WebFlux | Non-blocking, 10k+ conexões | Mudança de stack, incompatível com @Transactional atual |

## Justificativa

- Notificações são estritamente server→client: SSE é suficiente
- `EventSource` API reconecta automaticamente sem código adicional no cliente
- Compatível com HTTP/1.1 e funciona através de proxies reversos (com heartbeat)
- Menor complexidade que WebSocket para o caso de uso atual

## Trade-offs e riscos

- **Thread por conexão:** Tomcat default tem ~200 threads. Acima de ~150 conexões SSE simultâneas, requests comuns são afetados. Mitigação: heartbeat a 25s elimina conexões fantasma.
- **In-memory:** Emitters são por JVM. Multi-instância requer Redis Pub/Sub (v2.0).
- **Proxies:** Nginx e ALB fecham conexões idle após 60s. Heartbeat a 25s mantém a conexão viva.

## Consequências

- Endpoint `/stream` exposto como `text/event-stream`
- `NotificationSseService` mantém `ConcurrentHashMap<UUID, List<SseEmitter>>`
- `@Scheduled(fixedDelay=25_000)` envia heartbeat comment a cada 25s
- Gauge `notifications.sse.connections.active` monitora carga

## Evolução planejada

**v1.2:** Migrar para `Flux<ServerSentEvent>` no Spring WebFlux para suportar 10k+ conexões sem bloqueio de threads.  
**v2.0:** Redis Pub/Sub para fanout cross-instance em cluster com múltiplas JVMs.
