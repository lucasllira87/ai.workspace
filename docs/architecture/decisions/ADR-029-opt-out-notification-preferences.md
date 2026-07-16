# ADR-029: Modelo opt-out para preferências de notificação

**Status:** Accepted  
**Date:** 2026-07-15  
**Module:** notifications

---

## Contexto

O módulo precisa definir o comportamento padrão quando um usuário ainda não configurou suas preferências de notificação, e ao introduzir novos tipos de notificação em versões futuras.

## Decisão

Adotar o modelo **opt-out** (habilitado por padrão): toda notificação é enviada por padrão em todos os canais. O usuário precisa explicitamente desativar tipos/canais indesejados.

## Alternativas avaliadas

| Opção | Prós | Contras |
|---|---|---|
| Opt-in | Usuário controla explicitamente | Baixo alcance inicial, notificações críticas podem ser perdidas |
| Opt-out | Alto alcance, notificações críticas chegam sempre | Risco de spam se mal usado |
| Todos habilitados sem preferências | Simplicidade | Sem controle para o usuário |

## Justificativa

- Notificações como `PAYMENT_FAILED` e `QUOTA_EXCEEDED` são operacionalmente críticas — opt-in as tornaria opcionais com risco de usuário não ver avisos importantes
- `NotificationPreference.isEnabled()` retorna `true` quando o tipo não existe no mapa, garantindo que novos `NotificationType` adicionados no futuro sejam automaticamente habilitados sem necessidade de migração de dados
- Novos usuários sem preferência registrada recebem todas as notificações imediatamente

## Implementação

```java
public boolean isEnabled(NotificationType type, NotificationChannel channel) {
    Set<NotificationChannel> channels = settings.get(type);
    if (channels == null) return true; // opt-out: sem entrada = habilitado
    return channels.contains(channel);
}
```

## Trade-offs e riscos

- **Risco de spam:** Se eventos forem disparados em loop (bug de billing), todos os usuários receberão emails. Mitigação v1.1: rate limiting por `(userId, type)` com Redis TTL.
- **Expectativa do usuário:** Alguns usuários preferem opt-in. Mitigação: UI de preferências clara com toggle por tipo/canal.

## Consequências

- `createDefault()` pré-popula o mapa com todos os `NotificationType` e todos os `NotificationChannel` habilitados
- Preferências são criadas lazily (primeira leitura cria o default)
- `update(type, enabledChannels)` permite desativar um tipo específico passando `Set.of()` (conjunto vazio)
