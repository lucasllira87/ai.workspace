# ADR-027: Idempotência de webhooks Stripe via externalId

**Status:** Accepted  
**Data:** 2026-07-15  
**Módulo:** billing

## Contexto

Stripe pode reenviar o mesmo webhook múltiplas vezes (retry em caso de timeout, falha de rede). O handler deve ser idempotente — processar o mesmo evento duas vezes não deve criar cobranças duplicadas ou corromper estado.

## Decisão

`WebhookService` verifica `externalId` antes de aplicar qualquer mutação:
- Para `invoice.payment_succeeded`: busca `Invoice` por `externalId` antes de `markPaid()`. Se não encontrado, loga aviso e retorna sem erro (Stripe pode entregar eventos de faturas que não existem no sistema ainda).
- `Invoice.markPaid()` é idempotente: retorna sem efeito se status já é `PAID`.
- `Subscription.renew()` não é idempotente por design — chamado apenas quando invoice é encontrada.

## Justificativa

- Stripe garante "at-least-once delivery" mas não "exactly-once" — idempotência é obrigatória
- Usar `externalId` como chave de deduplicação é mais confiável que timestamps
- `Invoice.markPaid()` com early return para PAID já garante idempotência em nível de domain

## Consequências

- Webhook unknown (evento para `externalId` inexistente) é logado como aviso e retorna 200 — Stripe não faz retry de webhooks com resposta 2xx.
- Para garantia total em alta escala, adicionar tabela `webhook_events(external_event_id, processed_at)` em v1.1.
