# ADR-003 — Strategy Pattern para Abstração de Provedores de IA

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

O AI Workspace integrará múltiplos provedores de IA (Claude, OpenAI, Gemini). A regra de negócio dos módulos `document-assistant` e `learning-platform` não deve conhecer qual provedor está sendo utilizado, permitindo troca sem alteração do domínio.

## Decisão

Adotamos o **Strategy Pattern** com uma porta de saída (`AIProviderPort`) definida na camada de aplicação do módulo `ai-core`.

```
AIProviderPort (interface — camada application/port/out)
├── ChatResponse chat(ChatRequest request)
├── String summarize(String content)
├── List<Question> generateQuestions(String content)
└── List<Flashcard> generateFlashcards(String content)

Adaptadores (camada infrastructure/adapter):
├── ClaudeAIAdapter    → implementa AIProviderPort
├── OpenAIAdapter      → implementa AIProviderPort
└── GeminiAdapter      → implementa AIProviderPort

AIProviderFactory
└── AIProviderPort resolve(String providerName)
```

O provedor ativo é definido em `application.properties`:
```
ai.provider.default=claude
```

O provedor pode ser configurado por usuário no futuro (preferência salva em `users`).

## Alternativas Consideradas

### Opção A — Integrar diretamente com Claude API nos use cases
- **Contras:** Acopla regra de negócio ao provedor; troca exige alteração em múltiplos lugares
- **Decisão:** Rejeitado

### Opção B — Strategy Pattern com AIProviderPort ✅
- **Prós:** Troca de provedor via configuração; testável com mock; extensível para novos provedores
- **Decisão:** Aceito

## Consequências

- Adicionar novo provedor = criar novo Adapter + registrar no Factory
- Use cases de `document-assistant` e `learning-platform` dependem apenas de `AIProviderPort`
- Permite A/B testing entre provedores no futuro
- Facilita fallback: se um provedor falhar, Factory pode tentar o próximo
