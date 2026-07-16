# ADR-031: Dispatch assíncrono de notificações via @Async listeners

**Status:** Accepted  
**Date:** 2026-07-15  
**Module:** notifications

---

## Contexto

Os listeners de billing, learning e documents são acionados via `@TransactionalEventListener(AFTER_COMMIT)`. Na implementação inicial, o dispatch de notificações (incluindo entrega SMTP) ocorria na thread do evento — ou seja, na thread que esperava o commit da transação originadora. Isso bloqueava o Stripe webhook por até 5 segundos enquanto o SMTP completava.

## Decisão

Anotar todos os listener methods com `@Async`, fazendo com que o dispatch de notificações ocorra em um thread pool separado após o commit da transação originadora.

## Fluxo após a decisão

```
Billing Webhook Request Thread
  → WebhookService.handlePaymentFailed() [TX aberta]
    → Invoice.markFailed() [domain event registrado]
  → TX commit
  → Spring agenda BillingEventNotificationListener.onPaymentFailed() [async, thread pool]
  → Request Thread retorna 200 OK para Stripe ← imediato

[Thread pool - separado]
  → onPaymentFailed() executa
    → NotificationService.send() [@Transactional — nova TX]
      → templateRenderer.render() [Thymeleaf]
      → emailSender.send() [SMTP 2-5s]
      → notification.markSent()
      → notificationRepository.save()
    → TX commit → domain event publicado → SSE push
```

## Alternativas avaliadas

| Opção | Prós | Contras |
|---|---|---|
| Síncrono (original) | Simples | Bloqueia thread do webhook, timeout Stripe |
| @Async nos listeners | Desacopla SMTP do webhook, simples | Sem garantia de entrega se JVM falhar |
| Outbox Pattern | Guaranteed delivery | Complexidade alta para v1.0 |
| Message Broker (Kafka) | Durável, escalável | Over-engineering para v1.0 |

## Justificativa

- `@Async + @TransactionalEventListener(AFTER_COMMIT)` é o padrão Spring para este cenário
- O listener `@Async` não tem acesso à transação original (já commitada), o que é correto — `NotificationService.send()` abre sua própria transação
- Stripe espera resposta HTTP em 5 segundos; entrega de email SMTP levaria além desse threshold sem `@Async`
- `@EnableAsync` adicionado em `NotificationsConfig` isola o escopo ao módulo

## Trade-offs e riscos

- **Lost events on crash:** Se a JVM cair após o commit mas antes de o thread async ser executado, a notificação não é enviada. Risco baixo em v1.0 com hardware confiável. **Mitigação v2.0:** Outbox Pattern.
- **Thread pool sizing:** O pool default do Spring (`SimpleAsyncTaskExecutor`) cria threads ilimitadas. Para produção, configurar um `ThreadPoolTaskExecutor` com limites definidos.
- **Exception swallowing:** `sendSafely()` captura e loga exceções sem propagar. Falhas de notificação não afetam a transação originadora. Comportamento intencional — uma falha de email não deve reverter uma cobrança.

## Consequências

- `@EnableAsync` em `NotificationsConfig`
- `@Async` em todos os métodos `@TransactionalEventListener` dos três listeners
- Thread pool padrão do Spring; configuração explícita planejada para v1.1
- `sendSafely()` garante que exceções de notificação não propagam para o chamador

## Known gaps documentados

- `emailForUser()` em `SecurityNotificationUserContextAdapter` lança `UnsupportedOperationException`. O canal EMAIL permanece inativo até implementação de `UserLookupPort` no módulo IAM (roadmap v1.1).
- Sem idempotência de eventos: duplicatas de `PaymentFailedEvent` geram emails duplicados. Mitigação v1.1: campo `externalRef` + `UNIQUE` constraint.
