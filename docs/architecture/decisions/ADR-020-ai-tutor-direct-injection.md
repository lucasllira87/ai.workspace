# ADR-020: AI Tutor usa injeção direta de conteúdo em vez de RAG

**Status:** Accepted  
**Data:** 2026-07-15  
**Módulo:** learning

## Contexto

O AI Tutor precisa responder perguntas dos alunos com base no conteúdo de uma lesson específica. Havia duas alternativas: (1) indexar o conteúdo da lesson no vector store e usar RAG para recuperar chunks relevantes, ou (2) injetar o conteúdo completo da lesson diretamente no prompt do LLM.

## Decisão

Injetar o conteúdo completo da lesson diretamente no contexto do LLM (prompt injection).

## Justificativa

- Lessons têm tipicamente ≤5000 tokens — cabe no context window dos modelos modernos com folga
- Injeção direta garante que o tutor tem acesso a todo o conteúdo, não apenas aos chunks mais similares semanticamente à pergunta
- Elimina latência e complexidade da busca vetorial para este caso de uso
- RAG é sub-ótimo para texto curto e coeso — chunks podem fragmentar explicações dependentes de contexto
- O conteúdo da lesson já está carregado no aggregate Course em memória — zero custo adicional de I/O

## Consequências

- Para lessons muito longas (> 50k tokens), o custo de tokens aumenta linearmente. Nesse caso, a roadmap v1.1 pode indexar via `AIIngestionPort` (reutilizando o pipeline do módulo documents) e usar RAG seletivo.
- Prompt injection guard (`===BEGIN_LESSON===` / `===END_LESSON===`) é obrigatório para prevenir que conteúdo da lesson seja interpretado como instrução pelo LLM.
