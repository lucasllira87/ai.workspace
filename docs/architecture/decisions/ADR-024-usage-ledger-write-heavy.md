# ADR-024: UsageLedger como aggregate write-heavy com totais denormalizados

**Status:** Accepted  
**Data:** 2026-07-15  
**Módulo:** billing

## Contexto

O `UsageLedger` precisa rastrear consumo de tokens e documentos por usuário por período. Cada chamada de AI gera um `UsageEntry`. Havia duas abordagens: (1) Event Sourcing — recalcular totais somando todas as entries, (2) Aggregate com totais denormalizados — manter `totalTokens`, `totalDocuments`, `totalStorageBytes` atualizados a cada `record()`.

## Decisão

Aggregate com totais denormalizados diretamente na tabela `usage_ledgers`. Cada `UsageLedger.record()` atualiza os totais no aggregate e persiste junto com a nova entry.

## Justificativa

- `CheckQuota` é chamado em todo request de AI — precisa ser O(1), não O(n entries)
- Event Sourcing requereria recalcular sobre potencialmente milhares de entries para verificar quota
- Os totais são derivados das entries — não há discrepância de verdade, apenas redundância de leitura
- Entries são append-only e imutáveis após persistidas

## Consequências

- `UsageLedger.toDomain()` no adapter omite entries (LAZY) quando só totais são necessários — economia de memória
- Reconciliação de totais vs entries via job de verificação pode ser adicionada em v1.1
- Risco de inconsistência em caso de falha parcial é mitigado pelo `@Transactional` em `UsageService.record()`
