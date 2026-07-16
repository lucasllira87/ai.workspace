# ADR-015: Prompt Template Engine — Versionamento e Renderização

**Status:** Aceito  
**Data:** 2026-07-14  
**Módulo:** ai-core

---

## Contexto

Prompts de IA são código. Uma mudança não versionada em um prompt pode degradar silenciosamente a qualidade de respostas em produção. O sistema precisa de:
1. Prompts reutilizáveis entre módulos (documents, learning)
2. Versionamento para rollback e comparação
3. Organização por domínio (documents, learning, generic)
4. Substituição de variáveis sem lógica de negócio embutida no template

## Decisão

`PromptTemplate` é um **Aggregate Root** no domínio do ai-core:

```
PromptTemplate
  - id: UUID
  - name: String (identificador semântico: "document-summarization")
  - domain: String (organização: "documents", "learning")
  - version: int (incrementa a cada atualização)
  - content: String (template com {{variáveis}})
  - variables: List<String> (variáveis obrigatórias declaradas)
  - active: boolean (apenas um ativo por nome)
```

**Renderização:** `PromptTemplate.render(Map<String, Object> vars)` valida que todas as variáveis declaradas estão presentes (lança `MissingTemplateVariableException` se não) e substitui `{{variável}}` pelo valor.

**Persistência:** Tabela `aicore.prompt_templates` com constraint `UNIQUE(name, version)`. Templates são imutáveis após criação — uma atualização cria nova versão.

**Acesso:** `PromptTemplateRepository.findActiveByName(name)` retorna sempre a versão ativa. `findByNameAndVersion(name, version)` permite acesso a versões anteriores para rollback ou análise.

## Alternativas descartadas

**A: Templates como arquivos `.txt` no classpath:**  
Descartado — não versionados no banco, impossível atualizar sem redeploy, sem rastreabilidade.

**B: Jinja2 / Thymeleaf para templates:**  
Descartado — engines com lógica (if/for) incentivam colocar regras de negócio no template. `{{variável}}` força separação clara.

**C: Template como código Java (String format):**  
Descartado — sem versionamento, sem reutilização, não pode ser editado por não-desenvolvedores.

**D: LangChain4j PromptTemplate:**  
Descartado — dependência pesada, lock-in com ecossistema específico de LangChain.

## Trade-offs

| Vantagem | Desvantagem |
|---|---|
| Templates auditáveis no banco | Mudanças de template exigem INSERT, não edição de arquivo |
| Rollback por versão | Sem lógica condicional no template (intencional) |
| Reutilizáveis entre módulos via nome | Cache necessário para evitar query a cada render |
| Domínio organiza templates por bounded context | — |

## Segurança

A sintaxe `{{variável}}` não interpreta código — é substituição literal. Prompt injection via variáveis controladas pelo usuário ainda é possível e deve ser mitigado com `PromptSanitizer` (v1.1).

## Evolução prevista

**v1.1:**
- Cache Redis: `@Cacheable(cacheNames = "prompt-templates", key = "#name")` em `DefaultPromptEngine`
- Endpoint de admin para CRUD de templates com autenticação

**v1.2:**
- A/B testing: duas versões ativas com traffic split controlado por feature flag
- Métricas por template: `avg_response_quality`, `avg_tokens`, `avg_cost` coletados via feedback dos módulos consumidores
- `PromptChain` — sequência de templates com output de um como variável do próximo
