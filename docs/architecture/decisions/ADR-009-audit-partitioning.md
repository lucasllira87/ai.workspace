# ADR-009 — Particionamento da Tabela de Auditoria por Ano

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

Eventos de auditoria crescem indefinidamente. Uma tabela monolítica acumula anos de registros, degradando performance de queries e tornando purgas (data retention) custosas e arriscadas.

Requisitos:
- Consultas de auditoria são quase sempre por período (últimos 30 dias, últimos 6 meses)
- Compliance pode exigir retenção de N anos e descarte após
- Volume estimado: 100-1000 usuários no MVP; potencial de millions de eventos/ano em produção

## Decisão

`audit.audit_events` usa **PARTITION BY RANGE(occurred_at)** com partições anuais criadas antecipadamente.

```sql
CREATE TABLE audit.audit_events (
    id          UUID NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    ...
    PRIMARY KEY (id, occurred_at)
) PARTITION BY RANGE (occurred_at);

CREATE TABLE audit.audit_events_2026
    PARTITION OF audit.audit_events
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
```

## Alternativas Rejeitadas

**Tabela única sem particionamento:** Simples de implementar. Inaceitável a longo prazo: VACUUM, DELETE e queries lentas em tabelas grandes.

**Particionamento por mês:** Granularidade maior facilita purge de dados antigos mês a mês, mas gera mais objetos no catálogo PostgreSQL e overhead operacional. Para o volume projetado, anual é suficiente.

**Particionamento por usuário (HASH):** Útil para queries por usuário específico. Incompatível com queries de compliance por período.

## Consequências

**Positivo:**
- `WHERE occurred_at BETWEEN :start AND :end` usa pruning automático — apenas partições relevantes são scaneadas
- Arquivamento/purge de dados antigos: `DROP TABLE audit.audit_events_2024` é instantâneo vs `DELETE FROM` em milhões de linhas
- VACUUM opera por partição, sem bloquear o restante

**Negativo / Atenção:**
- Partições futuras devem ser criadas antes do ano virar — implementar rotina automática ou incluir no checklist de deploy anual
- A chave primária composta `(id, occurred_at)` é necessária — PostgreSQL exige que a coluna de particionamento faça parte da PK
- Queries sem filtro de data escaneiam todas as partições (evitar na aplicação)

## Operação

Criar próxima partição com antecedência (ex: novembro de cada ano):
```sql
CREATE TABLE audit.audit_events_2029
    PARTITION OF audit.audit_events
    FOR VALUES FROM ('2029-01-01') TO ('2030-01-01');
```

Migração V008 cria partições para 2026, 2027 e 2028 — suficiente para o MVP e os próximos anos.
