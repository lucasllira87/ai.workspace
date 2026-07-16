# ADR-007 — Versionamento de API

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

A API será consumida pelo frontend React e, futuramente, por clientes mobile e integrações B2B. Precisamos de uma estratégia que permita evoluir contratos sem quebrar clientes existentes.

## Decisão

Versionamento via **prefixo de path**: `/api/v1/`

Todos os endpoints seguem o padrão:
```
/api/v1/{módulo}/{recurso}

Exemplos:
POST /api/v1/auth/login
GET  /api/v1/documents
POST /api/v1/documents/{id}/chat
GET  /api/v1/learning/materials
```

**Critério para nova versão:** uma mudança que quebra contratos existentes (remoção de campo, alteração de tipo, mudança de semântica) exige nova versão (`/api/v2/`). Adições backward-compatible (novo campo opcional) não exigem nova versão.

## Alternativas Rejeitadas

- **Header versioning** (`Accept: application/vnd.api+json;version=1`): menos visível, dificulta testes e documentação
- **Sem versionamento**: inviabiliza evolução sem breaking changes

## Consequências

- Swagger UI organizado por versão
- Facilita deprecação gradual de endpoints antigos
- Clients sabem exatamente qual contrato estão consumindo
