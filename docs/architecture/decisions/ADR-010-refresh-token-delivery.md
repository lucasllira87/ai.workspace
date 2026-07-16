# ADR-010 — Entrega do Refresh Token: Response Body (MVP) → HttpOnly Cookie (Produção)

**Data:** 2026-07-14  
**Status:** Aceito (com migração planejada)  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

O refresh token precisa ser persistido no cliente entre sessões. Existem duas abordagens principais:

1. **Response Body (JSON):** O token é retornado no payload da resposta e armazenado pelo cliente (tipicamente em `localStorage` ou `sessionStorage`)
2. **HttpOnly Cookie:** O servidor emite o cookie via `Set-Cookie` com flags `HttpOnly`, `Secure`, `SameSite=Strict`

## Decisão

**MVP:** Entrega via **response body** (JSON). Mais simples de implementar em SPA React sem configuração de CORS para cookies.

**Produção (v1.1+):** Migrar para **HttpOnly Cookie**. Atualmente documentado mas não implementado.

## Análise de Segurança

### Response Body → localStorage
- **Vulnerável a XSS:** Qualquer JavaScript na página pode ler `localStorage`. Um ataque XSS bem-sucedido extrai o refresh token e persiste o acesso mesmo após rotação do access token.
- Não requer configuração adicional de servidor ou CORS
- Simples de implementar no frontend (armazenar/recuperar de localStorage)

### HttpOnly Cookie
- **Imune a XSS:** JavaScript não consegue ler cookies `HttpOnly`. Um ataque XSS pode usar o token (via requests automáticos do browser), mas não pode exfiltrá-lo para outro servidor.
- **Vulnerável a CSRF:** Browser envia cookies automaticamente — requer proteção CSRF (token, SameSite, Double Submit Cookie)
- `SameSite=Strict` mitiga CSRF para a maioria dos casos
- Requer configuração de CORS correta com `credentials: true`
- Mais complexo de implementar e testar

## Trade-off MVP

No MVP com 100-1000 usuários e sem dados sensíveis críticos, o risco de XSS é gerenciado por:
- CSP (Content Security Policy) restritiva no frontend
- Ausência de `innerHTML` com dados do usuário
- Dependências auditadas

O access token tem TTL de 15min, limitando a janela de exposição mesmo se o localStorage for comprometido.

## Plano de Migração para Cookie (v1.1)

**Backend — mudanças em `AuthController`:**
```java
// Em login e refresh, ao invés de retornar refreshToken no body:
ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
    .httpOnly(true)
    .secure(true)                    // apenas HTTPS
    .sameSite("Strict")
    .path("/api/v1/auth/refresh")    // cookie scoped ao endpoint de refresh
    .maxAge(Duration.ofDays(7))
    .build();
response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

// Body retorna apenas o access token
return ResponseEntity.ok(ApiResponse.ok(new AuthResponse(accessToken, null, userResponse)));
```

**Backend — endpoint `/refresh`:**
```java
// Ler o refresh token do cookie em vez do @RequestParam
@CookieValue(name = "refresh_token") String refreshToken
```

**Frontend — mudanças:**
- Remover armazenamento de refreshToken no localStorage
- Fetch com `credentials: 'include'` para enviar cookies cross-origin

**CORS — adicionar:**
```yaml
allowed-credentials: true
allowed-origins: https://app.aiworkspace.com  # sem wildcard quando credentials=true
```

## Consequências (MVP)

- Refresh token exposto a XSS mitigado por CSP e boas práticas de frontend
- Token de refresh tem rotação automática — comprometimento de um token tem janela limitada
- Documentado como débito técnico a ser pago em v1.1
