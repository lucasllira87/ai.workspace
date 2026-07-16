# ADR-025: Quota enforcement síncrono no application layer

**Status:** Accepted  
**Data:** 2026-07-15  
**Módulo:** billing

## Contexto

Quota enforcement pode ser implementado em vários lugares: (1) Domain layer no aggregate, (2) Application layer antes da operação, (3) Infrastructure layer (aspect/interceptor), (4) API Gateway.

## Decisão

`CheckQuotaUseCase.enforceTokenQuota()` e `enforceDocumentQuota()` são chamados explicitamente no application layer de outros módulos via adapter — cada módulo define sua própria porta (ex: `QuotaCheckPort` em ai-core) e o adapter chama `CheckQuotaUseCase`.

## Justificativa

- Domain layer não deve conhecer billing — violaria separação de concerns
- Infrastructure aspect (AOP) é mais difícil de testar e debugar
- Application layer explícito é visível, rastreável em stack traces, e testável com mocks
- `enforceTokenQuota()` lança `QuotaExceededException` (400) — tratada pelo `GlobalExceptionHandler` compartilhado

## Consequências

- Cada módulo que consome AI deve chamar `enforceTokenQuota()` antes da operação
- Não há race condition em lote — quota é verificada e registrada em transações separadas (verificar então usar, não atômico). Aceitável para v1.
- Para v2 com alta concorrência, considerar lock pessimista no ledger ou quota bucket com Redis.
