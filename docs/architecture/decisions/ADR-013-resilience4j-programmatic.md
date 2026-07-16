# ADR-013: Resilience4j Programático (sem anotações)

**Status:** Aceito  
**Data:** 2026-07-14  
**Módulo:** ai-core

---

## Contexto

O `AIOrchestrator` precisa aplicar Circuit Breaker e Retry em chamadas a providers de IA. Resilience4j suporta dois modos: anotações (`@CircuitBreaker`, `@Retry`) e configuração programática via Registry.

O desafio: o provider é selecionado dinamicamente em runtime pelo `FallbackChain`. Não há como usar anotações em métodos que não sabem qual provider serão chamados no compile time.

## Decisão

Usar Resilience4j programaticamente via `CircuitBreakerRegistry` e `RetryRegistry`:

```java
CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(provider.getProviderId().value());
Retry retry = retryRegistry.retry("ai-provider");

Supplier<ChatResponse> decorated = CircuitBreaker.decorateSupplier(cb,
    Retry.decorateSupplier(retry, () -> provider.chat(request)));
```

Um `CircuitBreaker` por provider (nomeado pelo `ProviderId`) — permite que OpenAI entre em open state sem afetar Anthropic.

Um único `RetryConfig` "ai-provider" compartilhado — todos os providers têm o mesmo backoff: 1s → 2s → 4s, 3 tentativas.

## Alternativas descartadas

**A: `@CircuitBreaker(name = "openai")` + `@Retry` em métodos fixos:**  
Descartado — incompatível com seleção dinâmica de provider. Exigiria um método anotado por provider.

**B: Spring AOP customizado:**  
Descartado — complexidade desnecessária quando o Resilience4j já oferece API programática.

**C: Retry manual com `Thread.sleep()`:**  
Descartado — não tem backoff configurável, não integra com Micrometer, não tem sliding window para CB.

## Trade-offs

| Vantagem | Desvantagem |
|---|---|
| Circuit breaker por provider — falha isolada | Mais verboso que anotações |
| Seleção dinâmica de provider possível | Requer `AtomicInteger` para contar tentativas |
| `CircuitBreakerRegistry` cria CB sob demanda | Configuração única (não por provider) |

## Lacunas para v1.1

- Adicionar `TimeLimiterRegistry` — timeout por chamada (30s)
- Adicionar `BulkheadRegistry` — semáforo por provider (max 10 chamadas concorrentes)
- Integrar `resilience4j-micrometer` para métricas automáticas no Prometheus

## Parâmetros de produção recomendados

```yaml
# Recomendados para tráfego médio (1k req/h)
circuit-breaker:
  slowCallDurationThreshold: 5s  # atual: 10s — reduzir
  slidingWindowSize: 20           # atual: 10 — aumentar
retry:
  maxWaitDuration: 4s             # limitar backoff máximo
```
