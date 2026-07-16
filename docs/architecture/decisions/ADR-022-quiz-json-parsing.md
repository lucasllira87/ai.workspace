# ADR-022: Quiz parsing via extração de array JSON por delimitadores

**Status:** Accepted  
**Data:** 2026-07-15  
**Módulo:** learning

## Contexto

O `AIQuizPort` precisa que o LLM retorne uma lista de `QuizQuestion` em formato estruturado. LLMs frequentemente adicionam texto extra antes ou depois do JSON (ex: "Here are the questions:" ou "I hope these help!"), quebrando parsers que esperam JSON puro.

## Decisão

`AICoreQuizAdapter` extrai o array JSON da resposta usando `response.indexOf('[')` e `response.lastIndexOf(']')`, depois deserializa o substring com Jackson `ObjectMapper`.

## Justificativa

- Robusto contra texto extra do LLM antes e depois do array
- Simples de implementar e auditar — nenhuma regex complexa
- Validação explícita dos campos obrigatórios pós-parse (`question`, `options`, `correctOptionIndex`) com `DomainException` descritiva
- Alternativa (JSON mode / structured outputs) depende de feature específica do provider — quebraria a abstração de `ChatProvider` agnóstica de provider

## Consequências

- Se o LLM retornar múltiplos arrays JSON, `lastIndexOf(']')` captura o maior array — geralmente o correto
- Em caso de parse failure, `DomainException` com mensagem clara é propagada ao cliente (HTTP 400)
- A abordagem não valida o número de opções via JSON schema — validação é feita em código (`options.size() < 2`)
