# ADR-012: Estratégia Multi-Provider com Interface Segregation

**Status:** Aceito  
**Data:** 2026-07-14  
**Módulo:** ai-core

---

## Contexto

O `ai-core` precisa suportar múltiplos provedores de IA (OpenAI, Anthropic, Google Gemini, Ollama) com capacidades heterogêneas:
- OpenAI: chat + embeddings + moderação
- Anthropic: apenas chat
- Google: chat + embeddings
- Ollama: chat + embeddings (local)

A decisão é como modelar essas diferenças no sistema de portas sem forçar providers a implementar capacidades que não possuem.

## Decisão

Aplicar ISP (Interface Segregation Principle) com interfaces separadas por capacidade:

```
ChatProvider          → chat(ChatRequest) : ChatResponse
EmbeddingProvider     → embed(EmbeddingRequest) : EmbeddingResponse
CompletionProvider    → complete(CompletionRequest) : CompletionResponse
ModerationProvider    → moderate(ModerationRequest) : ModerationResponse
ImageGenerationProvider → generate(ImageRequest) : ImageResponse
```

Cada provider implementa apenas as interfaces que suporta. O `ProviderRegistry` coleta todas as implementações via `@Autowired(required = false) List<T>` e expõe `findChatProvider(ProviderId)` e `findEmbeddingProvider(ProviderId)`.

O `FallbackChain` lê a ordem de preferência de `AIConfigPort.getChatFallbackOrder()` e retorna a lista ordenada de providers disponíveis. O `AIOrchestrator` itera essa lista, tentando o próximo em caso de falha.

## Alternativas descartadas

**A: Interface única com métodos opcionais (`throws UnsupportedOperationException`):**  
Descartado — viola LSP e cria contratos frágeis. O chamador não sabe o que o provider suporta sem testar.

**B: Herança de `AbstractProvider`:**  
Descartado — acoplamento de implementação, impede providers de SDK externos (que já têm sua hierarquia).

**C: Spring `@Qualifier` + string de provider:**  
Descartado — perde type-safety, acoplamento por string.

## Trade-offs

| Vantagem | Desvantagem |
|---|---|
| Adicionar provider = 1 arquivo | Mais interfaces para manter |
| LSP garantido por tipo | Descoberta de capacidade exige `instanceof` ou Registry |
| `@ConditionalOnProperty` = zero impacto se desabilitado | FallbackChain só itera providers com mesma capacidade |

## Riscos

- Typo no `ai.fallback-order.chat` no YAML resulta em provider ignorado silenciosamente. Mitigação: validar no startup que todos os providers listados estão registrados.

## Evolução prevista

Em v1.1, adicionar `getSupportedCapabilities(): Set<Capability>` ao registry para introspection e health checks por capacidade.
