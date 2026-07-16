# IAM Module — Evolution Roadmap

**Data:** 2026-07-14

Este documento descreve o plano de evolução do módulo IAM após o MVP, cobrindo os eixos de segurança, autenticação avançada e extração para microsserviço.

---

## Estado Atual (MVP)

| Funcionalidade | Status |
|---|---|
| Registro de usuário | ✅ Implementado |
| Login com e-mail + senha | ✅ Implementado |
| JWT access token (HS256, 15min) | ✅ Implementado |
| Refresh token rotativo (PostgreSQL) | ✅ Implementado |
| Logout com verificação de posse | ✅ Implementado |
| BCrypt strength=12 | ✅ Implementado |
| Proteção contra timing attack | ✅ Implementado |
| Claims iss/aud no JWT | ✅ Implementado |
| Swagger desabilitado em prod | ✅ Implementado |
| Rate limiting | ❌ Não implementado |
| MFA | ❌ Não implementado |
| E-mail de boas-vindas/confirmação | ❌ No-op (log) |
| HttpOnly cookie | ❌ Não implementado |

---

## v1.1 — Segurança Operacional (pré go-live)

**Objetivo:** Nível de segurança adequado para usuários reais.

### 1. Rate Limiting com Redis

Sliding window counter por IP e por e-mail:

```java
// RateLimitingFilter.java (infrastructure)
// Endpoints protegidos: /login, /register, /refresh
// Limites sugeridos:
//   Por IP: 20 tentativas/minuto em /login
//   Por e-mail: 5 tentativas/minuto em /login
//   Por IP: 10 tentativas/hora em /register

String key = "rate_limit:login:ip:" + ipAddress;
Long attempts = redisTemplate.opsForValue().increment(key);
if (attempts == 1) {
    redisTemplate.expire(key, 60, TimeUnit.SECONDS);
}
if (attempts > 20) {
    throw new TooManyRequestsException("Too many login attempts");
}
```

HTTP 429 com header `Retry-After` quando limite excedido.

### 2. HttpOnly Cookie para Refresh Token

Ver ADR-010. Mudanças principais:
- `AuthController.login()` emite `Set-Cookie: refresh_token=...; HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth/refresh`
- `AuthController.refresh()` lê `@CookieValue("refresh_token")`
- Frontend remove armazenamento em localStorage
- CORS: `credentials: 'include'` + origem específica (sem wildcard)

### 3. Silent Registration

Endpoint `POST /register` retorna sempre HTTP 202 com mensagem genérica. E-mail enviado ao endereço fornecido com conteúdo diferenciado:
- Novo usuário: link de ativação
- Usuário existente: "Sua conta já existe, tente fazer login"

Requer `EmailPort` real implementado (SendGrid/SES).

### 4. E-mail de Confirmação de Conta

`NoOpEmailAdapter` → `SendGridEmailAdapter`:
```java
// EmailPort implementação real
sendWelcomeEmail(email, fullName, confirmationLink);
```

`UserStatus.PENDING_VERIFICATION` já existe na enum — ativar fluxo de verificação.

### 5. MFA via TOTP

Tabela nova: `iam.mfa_configs (user_id, secret, enabled_at, backup_codes JSONB)`.

Fluxo de login modificado:
1. Validar credenciais → retornar `mfa_required: true` com `mfa_session_token` (TTL 5min Redis)
2. `POST /api/v1/auth/mfa/verify` com código TOTP + `mfa_session_token`
3. Emitir access + refresh tokens após validação TOTP

Biblioteca sugerida: `dev.samstevens.totp:totp:1.7.1`.

---

## v1.2 — Autenticação Social e Multi-tenant

**Objetivo:** Reduzir fricção de cadastro e preparar para uso corporativo.

### 1. OAuth2 / Social Login

Spring Security OAuth2 Client. Provedores: Google, GitHub.

Fluxo:
1. Frontend redireciona para `/oauth2/authorization/google`
2. Callback em `/login/oauth2/code/google`
3. `OAuth2UserService` → busca/cria usuário local → emite JWT próprio
4. Tabela: `iam.social_identities (user_id, provider, provider_user_id, access_token_encrypted)`

### 2. RBAC com Permissões Granulares

Evolução de `Role` (USER/ADMIN) para modelo permission-based:
```
iam.permissions (id, name, description)
iam.role_permissions (role_id, permission_id)
iam.user_permissions (user_id, permission_id) -- overrides
```

Claims JWT: `"permissions": ["documents:read", "documents:write", "ai:query"]`.

### 3. Organizations / Workspaces

Multi-tenancy para uso em times:
```
iam.organizations (id, name, slug, plan)
iam.organization_members (org_id, user_id, role, invited_at, joined_at)
```

JWT claim: `"org_id": "..."`. Módulos de domínio filtram dados por `org_id`.

### 4. Passkeys / WebAuthn

Alternativa à senha para usuários que preferem biometria/chave de hardware:
- Tabela: `iam.passkeys (user_id, credential_id, public_key, sign_count, aaguid)`
- Biblioteca: `com.webauthn4j:webauthn4j-spring-security:0.x`

---

## v2.0 — Extração como Microsserviço

**Objetivo:** IAM como serviço independente usado por todos os outros serviços do workspace.

### 1. RS256 + JWKS Endpoint

Ver ADR-008. IAM expõe:
```
GET /.well-known/jwks.json
```

Outros serviços validam JWT localmente com chave pública — sem chamada ao IAM no hot path.

### 2. Spring Authorization Server

Implementar OAuth2 Authorization Server completo:
- Authorization Code Flow para clientes web
- Client Credentials Flow para serviço-a-serviço
- Token Introspection endpoint

### 3. SAML 2.0

SSO para empresas com IdP corporativo (Okta, Azure AD, ADFS):
- IAM como Service Provider (SP)
- Mapeamento de claims SAML → usuário local

### 4. Separação Física do Banco

Schema `iam` extraído para banco PostgreSQL dedicado:
- Configuração de datasource separada
- Flyway migrations próprias do serviço IAM
- Outros módulos não têm acesso direto à schema IAM

### 5. Audit Log via Mensageria

Substituir `ApplicationEventPublisher` (in-process) por Kafka/RabbitMQ:
```java
// UserRegisteredEvent → Kafka topic "iam.events"
// Outros serviços consomem o topic para atualizar seus próprios dados
```

---

## Matriz de Decisões por Versão

| Decisão | MVP | v1.1 | v1.2 | v2.0 |
|---|---|---|---|---|
| Algoritmo JWT | HS256 | HS256 | HS256 | RS256 |
| Entrega Refresh Token | JSON body | HttpOnly Cookie | HttpOnly Cookie | HttpOnly Cookie |
| Rate Limiting | ❌ | Redis sliding window | Redis sliding window | Redis + API Gateway |
| MFA | ❌ | TOTP | TOTP + Backup codes | TOTP + Passkeys |
| Social Login | ❌ | ❌ | Google + GitHub | Google + GitHub + SAML |
| Multi-tenant | ❌ | ❌ | Organizations | Organizations + Enterprise |
| E-mail real | No-op | SendGrid/SES | SendGrid/SES | SendGrid/SES |
| Banco IAM | Schema compartilhado | Schema compartilhado | Schema compartilhado | Banco dedicado |
