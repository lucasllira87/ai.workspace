# ADR-004 — Autenticação com JWT + Refresh Token Persistido

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

O sistema precisa de autenticação segura com suporte a logout efetivo, revogação de sessões, e preparação para múltiplos dispositivos por usuário.

## Decisão

**Access Token:** JWT de curta duração (15 minutos), stateless, assinado com RS256 (par de chaves RSA — mais seguro que HS256 em ambientes distribuídos futuros).

**Refresh Token:** UUID v4 opaco, persistido no PostgreSQL, duração de 7 dias, com rotação automática a cada uso. Enviado via cookie HttpOnly + Secure + SameSite=Strict.

### Fluxo Completo

```
1. Login   → gera access_token (JWT 15min) + refresh_token (UUID 7d, salvo no BD)
2. Request → Authorization: Bearer <access_token>
3. Expirou → POST /auth/refresh com cookie refresh_token
           → valida no BD → gera novo access_token + rotaciona refresh_token
4. Logout  → deleta refresh_token do BD → cookie limpo
```

### Estrutura da Tabela `refresh_tokens`

```sql
id            UUID PRIMARY KEY
token         VARCHAR(255) UNIQUE NOT NULL
user_id       UUID NOT NULL REFERENCES users(id)
device_info   VARCHAR(255)          -- futuro: múltiplos dispositivos
ip_address    VARCHAR(45)
expires_at    TIMESTAMP NOT NULL
revoked_at    TIMESTAMP             -- NULL = ativo
created_at    TIMESTAMP NOT NULL
```

### Claims do JWT (Access Token)

```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "roles": ["ROLE_USER"],
  "iat": 1234567890,
  "exp": 1234568790
}
```

## Alternativas Consideradas

### Opção A — Somente JWT (stateless puro)
- **Prós:** Sem estado no servidor; escalabilidade horizontal simples
- **Contras:** Sem logout efetivo; sem revogação de token comprometido; access token longo para compensar (risco maior)
- **Decisão:** Rejeitado

### Opção B — JWT + Refresh Token persistido ✅
- **Prós:** Logout efetivo; revogação possível; controle de dispositivos; alinhado com padrão corporativo
- **Contras:** Estado no servidor (Redis/BD para refresh tokens); ligeiramente mais complexo
- **Decisão:** Aceito

### Opção C — Sessions no Redis (stateful puro)
- **Prós:** Revogação imediata; sem JWT
- **Contras:** Requer Redis disponível para toda requisição; menos comum em APIs REST; dificulta futura arquitetura distribuída
- **Decisão:** Rejeitado

## Consequências

- Blacklist de tokens revogados não necessária: refresh tokens são consultados por lookup direto no BD
- Redis pode ser usado para cache do `user_id → roles` para evitar consulta no BD a cada request
- A rotação de refresh token invalida automaticamente tokens roubados (se o atacante usar o token, o legítimo é invalidado e o usuário precisa fazer login novamente)
