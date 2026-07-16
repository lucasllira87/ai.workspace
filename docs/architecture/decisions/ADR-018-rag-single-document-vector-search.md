# ADR-018: RAG Single-Document — Vector Search + Contexto + LLM Single-Turn

**Status:** Aceito  
**Data:** 2026-07-15  
**Módulo:** documents

---

## Contexto

O módulo documents precisa responder perguntas sobre o conteúdo de um documento específico. A abordagem deve:
- Ser relevante (não alucinar conteúdo não presente no documento)
- Citar as partes do documento usadas na resposta
- Funcionar sem requer fine-tuning de modelos
- Ser compatível com o `AIQueryPort` que abstrai o ai-core

---

## Decisão

Implementar RAG (Retrieval-Augmented Generation) single-document, single-turn:

1. **Embed pergunta:** `GenerateEmbeddingUseCase` converte a pergunta do usuário em vetor
2. **Vector search:** `VectorStore.similaritySearch(documentId, vector, topK, minSimilarity)` retorna chunks do documento com cosine similarity ≥ threshold
3. **Context construction:** chunks concatenados com separador `---`
4. **LLM call:** system prompt instrui o modelo a responder exclusivamente com base no contexto
5. **Response:** resposta + referências de chunks (chunkIndex, similarity, sectionTitle)

Parâmetros configuráveis: `documents.rag-top-k` (default 5) e `documents.rag-min-similarity` (default 0.7).

O isolamento por `documentId` no vector search garante que chunks de um documento nunca contaminem a resposta de outro.

---

## Alternativas Consideradas

### Full-document context (sem RAG)

Enviar o documento inteiro como contexto para o LLM.

**Vantagens:** sem step de retrieval; resposta potencialmente mais completa.  
**Desvantagens:** documentos de 50 MB excedem qualquer context window; custo de tokens proibitivo; latência alta.  
**Descartado:** inviável para documentos grandes.

### Fine-tuning por documento

**Vantagens:** resposta mais natural e integrada.  
**Desvantagens:** custo de fine-tuning por documento; tempo de processamento de horas; não escala para muitos documentos por usuário.  
**Descartado:** fora de escopo para plataforma multi-usuário.

### RAG multi-documento (corpus global)

Buscar chunks de todos os documentos do usuário simultaneamente.

**Vantagens:** responde perguntas que cruzam múltiplos documentos.  
**Desvantagens:** risco de relevância diluída; source attribution mais complexa; escopo cresce significativamente.  
**Descartado para MVP:** implementar como endpoint separado em v1.2.

---

## Consequências

**Positivas:**
- Fundamentado no documento — o system prompt instrui explicitamente a não inventar informações
- Source citation incluída na resposta — o cliente pode mostrar de onde veio a informação
- Parâmetros configuráveis — topK e minSimilarity ajustáveis por ambiente sem redeploy
- Compatível com qualquer provider (OpenAI, Anthropic, Google) via `ChatUseCase`

**Negativas / Trade-offs:**
- Single-turn: cada pergunta é independente, sem histórico de conversa. Para perguntas de follow-up ("e o próximo ponto?"), o usuário precisa ser explícito. Mitigação em v1.1: aceitar histórico via `ChatWithDocumentCommand`.
- Top-K limitado: chunks mais relevantes podem estar além do topK se o embedding space não capturar bem a nuance. Mitigação em v1.2: reranker cross-encoder.
- Sem prompt injection guard: conteúdo do documento inserido no system prompt. Risco documentado como DT-4 e DT-8. Mitigação em v1.1: bloco de contexto delimitado explicitamente.
