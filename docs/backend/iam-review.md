# IAM Module — Deep Architectural Review

**Data:** 2026-07-14  
**Revisor:** Claude (Tech Lead / Arquiteto)  
**Escopo:** Revisão completa do módulo IAM antes da aprovação e avanço para `ai-core`  
**Resultado:** ✅ Módulo aprovado com todas as issues críticas resolvidas

---

## Sumário Executivo

O módulo IAM foi submetido a uma revisão arquitetural profunda cobrindo segurança (OWASP Top 10), qualidade de código, aderência à Clean Architecture, performance e testabilidade. Foram identificados e resolvidos **2 bugs críticos**, **1 violação de Clean Architecture**, **1 duplicação de código** e **4 vulnerabilidades de segurança**. Não há bloqueadores remanescentes.

---

## 1. Revisão de Segurança (OWASP Top 10)

### S1 — Timing Attack / User Enumeration via Login ✅ CORRIGIDO
**Severidade:** Alta  
**Categoria:** A07 - Identification and Authentication Failures

**Problema:** Login retornava instantaneamente quando o e-mail não existia (< 1ms), e executava BCrypt (~250ms) quando existia. Diferença de tempo revelavae-mails cadastrados.

**Correção aplicada em `LoginService`:**
```java
private static final String DUMMY_HASH =
    "$2a$12$kZHhE8j3P4tGYqHd6J7b.OdSTwjLaqEbRSfJ4P2fkFG1k1j4OxTWm";

if (userOpt.isEmpty()) {
    passwordHasher.matches(command.password(), HashedPassword.ofHash(DUMMY_HASH));
    throw new UnauthorizedException("Invalid credentials");
}
```

**Ver também:** ADR-011

---

### S2 — Logout sem Verificação de Posse ✅ CORRIGIDO
**Severidade:** Alta  
**Categoria:** A01 - Broken Access Control

**Problema:** `LogoutService` revogava qualquer token pelo valor da string, sem verificar se pertencia ao usuário autenticado. Um usuário autenticado poderia encerrar sessões de qualquer outro usuário.

**Correção aplicada:**
- `LogoutCommand` recebe `authenticatedUserId`
- `LogoutService` verifica `token.getUserId().equals(UserId.of(command.authenticatedUserId()))`
- `AuthController.logout()` extrai userId do `Authentication.getPrincipal()`

---

### S3 — Mensagem Explícita de E-mail Duplicado no Registro ⚠️ ROADMAP v1.1
**Severidade:** Média  
**Categoria:** A07 - Identification and Authentication Failures

**Problema:** `RegisterUserService` lançava `ConflictException("Email already in use")` confirmando que o e-mail está cadastrado.

**Status MVP:** Mensagem genérica de ConflictException retornada pelo `GlobalExceptionHandler` sem confirmar o motivo específico. Aceitável para o MVP.

**Roadmap:** Implementar "silent registration" com e-mail diferenciado quando EmailPort real estiver disponível.

---

### S4 — Refresh Token no Response Body (localStorage vulnerável a XSS) ⚠️ ROADMAP v1.1
**Severidade:** Média  
**Categoria:** A02 - Cryptographic Failures / A07

**Problema:** Refresh token retornado no JSON body será armazenado em localStorage pelo frontend — legível por qualquer JavaScript na página (XSS).

**Mitigação no MVP:** CSP restritiva no frontend, TTL curto (15min) no access token, rotação do refresh token.

**Roadmap:** Migrar para `HttpOnly` cookie com `SameSite=Strict`. Detalhado em ADR-010.

---

### S5 — Ausência de Rate Limiting nos Endpoints de Auth ⚠️ ROADMAP v1.1
**Severidade:** Média  
**Categoria:** A07

**Problema:** Não há proteção contra força bruta em `/login`, `/register`, `/refresh`.

**Mitigação no MVP:** BCrypt strength=12 torna força bruta custosa (~250ms/tentativa).

**Roadmap:** Redis sliding window rate limiting por IP e por e-mail antes de go-live com usuários reais. Implementar em v1.1 com `RedisConfig` já disponível.

---

### S6 — Claims iss/aud ausentes no JWT ✅ CORRIGIDO
**Severidade:** Média  
**Categoria:** A07

**Problema:** JWT sem `issuer` e `audience` pode ser aceito por qualquer sistema que use a mesma chave — token replay cross-service.

**Correção em `JwtTokenAdapter`:**
```java
.issuer("ai-workspace")
.audience().add("ai-workspace-api").and()
// ...
.requireIssuer("ai-workspace")
```

---

### S7 — JWT Secret sem Validação de Comprimento Mínimo ✅ CORRIGIDO
**Severidade:** Média  
**Categoria:** A02

**Problema:** Segredo curto (< 256 bits) resultaria em chave fraca silenciosamente.

**Correção — @PostConstruct em `JwtTokenAdapter`:**
```java
@PostConstruct
void init() {
    byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < MIN_SECRET_BYTES) {
        throw new IllegalStateException(
            "JWT secret must be at least 256 bits (32 bytes). Current: " + keyBytes.length);
    }
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
}
```

---

### S8 — SQL Injection ✅ SEM ISSUE
Spring Data JPA com named parameters (`@Param`) e JPQL. Nenhum SQL concatenado encontrado.

### S9 — XSS ✅ SEM ISSUE (backend)
API REST retorna JSON — sem renderização de HTML no backend. Mitigação pertence ao frontend.

### S10 — CSRF ✅ ACEITO NO MVP
CSRF desabilitado em `SecurityConfig` (API stateless, sem cookie). Quando migrar para HttpOnly cookie (S4/ADR-010), CSRF deve ser reabilitado com `SameSite=Strict`.

### S11 — Swagger em Produção ✅ CORRIGIDO
`application-prod.yml` desativa `springdoc.swagger-ui.enabled` e `springdoc.api-docs.enabled`.

---

## 2. Code Review (Estilo PR)

### CA-1 — JwtProperties (infrastructure) usada na camada de aplicação ✅ CORRIGIDO
**Categoria:** Clean Architecture Violation

`LoginService` e `RefreshAccessTokenService` importavam `JwtProperties` de `infrastructure.config` — dependência proibida pela regra de dependência (Application → não pode importar Infrastructure).

**Correção:**
1. Criada `TokenPolicyPort` em `application/port/out/`
2. Criado `JwtTokenPolicyAdapter` em `infrastructure/config/` implementando a interface
3. Services agora injetam `TokenPolicyPort` (interface da própria camada)

---

### DUP-1 — Método `toDto(User)` duplicado em 3 services ✅ CORRIGIDO
**Categoria:** DRY Violation

Mesmo método de mapeamento `User → UserDto` existia em `RegisterUserService`, `LoginService` e `RefreshAccessTokenService`.

**Correção:** Criado `UserDtoMapper` como classe utilitária estática em `application/mapper/`.

---

### BUG-1 — UUID do UserProfile regenerado em toda persistência ✅ CORRIGIDO
**Severidade:** Crítico  
**Categoria:** Data Integrity Bug

**Problema:** `UserPersistenceMapper.toProfileEntity()` chamava `entity.setId(UUID.randomUUID())` toda vez que convertia o domínio para entidade JPA, gerando um novo UUID a cada save. Na segunda persistência, violaria a UNIQUE CONSTRAINT em `iam.user_profiles.user_id`.

**Correção:**
1. Adicionado campo `UUID profileId` na entidade de domínio `UserProfile`
2. `UserProfile.createDefault()` gera o UUID uma única vez
3. `UserProfile.reconstitute(UUID profileId, ...)` preserva o UUID existente ao reconstituir do banco
4. `UserPersistenceMapper.toProfileEntity()` usa `profile.getProfileId()` em vez de `UUID.randomUUID()`
5. `UserPersistenceMapper.toProfileDomain()` passa `entity.getId()` ao chamar `UserProfile.reconstitute()`

---

### BUG-2 — CascadeType.ALL ausente em UserJpaEntity.profile ✅ CORRIGIDO
**Severidade:** Crítico  
**Categoria:** ORM Configuration Bug

**Problema:** Sem `CascadeType.ALL`, salvar `UserJpaEntity` não propagaria para `UserProfileJpaEntity` — perfil seria silenciosamente perdido.

**Correção em `UserJpaEntity`:**
```java
@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
private UserProfileJpaEntity profile;
```

---

### INC-1 — Domain Events inconsistentes (não bloqueador) ℹ️ ANOTADO
`User.block()` e `User.activate()` não registram `DomainEvent`. Apenas `User.create()` via `UserRegisteredEvent`. Aceitável no MVP pois não há listeners para esses eventos. Documentado para revisão em v1.1.

---

## 3. Análise de Arquitetura

### Clean Architecture — Aderência por Camada

| Camada | Status | Observações |
|---|---|---|
| Domain | ✅ Zero dependências externas | Records, interfaces puras, sem anotações Spring |
| Application | ✅ Após CA-1 corrigido | Depende apenas de domain + interfaces próprias |
| Infrastructure | ✅ Implementa ports | JPA, Redis, JWT, BCrypt — todos atrás de interfaces |
| Presentation | ✅ | Depende apenas de application (use cases + commands + DTOs) |

### Hexagonal Architecture — Ports Identificadas

**Inbound (Driving):**
- `RegisterUserUseCase`, `LoginUseCase`, `RefreshAccessTokenUseCase`, `LogoutUseCase`

**Outbound (Driven):**
- `UserRepository` (persistence)
- `RefreshTokenRepository` (persistence)
- `PasswordHasher` (security)
- `TokenGeneratorPort` (JWT)
- `TokenPolicyPort` (configuration)
- `EmailPort` (notification)

### Readiness para Extração como Microsserviço

O módulo IAM pode ser extraído com as seguintes mudanças:
1. Substituir `ApplicationEventPublisher` (Spring) por mensageria (Kafka/RabbitMQ)
2. Migrar HS256 → RS256 com JWKS endpoint (ADR-008)
3. Separar banco de dados — schemas JPA já isolados (`schema="iam"`)
4. Outros módulos passam a validar JWT via chave pública sem chamar o IAM

---

## 4. Análise de Performance

### BCrypt strength=12
Custo: ~250ms por hash. Aceitável para autenticação. Alto para endpoints com alto volume (registro em massa). No MVP, sem risco.

### N+1 Query Risk
`UserRepositoryAdapter.findById()` carrega `UserProfile` via `FetchType.LAZY`. Quando `toUserDto()` acessa `user.getProfile()`, dispara segunda query. Mitigação: adicionar `@EntityGraph` ou `JOIN FETCH` quando performance for medida. Documentado para otimização posterior.

### Índices Criados (V009)
- `idx_users_email` — suporta `findByEmail()` — principal path de login
- `idx_refresh_tokens_token` — suporta `findByToken()` — path de refresh e logout
- `idx_refresh_tokens_user_id` — suporta `findActiveByUserId()` e `revokeAllByUserId()`

---

## 5. Análise de Testabilidade

### Pontos Positivos
- Domain completamente isolado — testável sem Spring, sem banco, sem mocks de framework
- Application services testáveis com mocks das ports (interfaces puras)
- `PasswordHasher` como interface permite `FakePasswordHasher` nos testes de aplicação
- `TokenGeneratorPort` como interface permite geração determinística nos testes

### Coverage Esperado por Camada
- Domain: 100% unit tests (sem dependências externas)
- Application: 90%+ unit tests com mocks
- Infrastructure: Integration tests com Testcontainers (PostgreSQL + Redis)
- Presentation: @WebMvcTest com SecurityConfig

### Fixtures Recomendadas (para implementação dos testes)
```java
// UserMother.java
public class UserMother {
    public static User activeUser() {
        return User.reconstitute(UserId.generate(), new Email("test@example.com"),
            HashedPassword.ofHash("$2a$12$..."), new FullName("Test User"),
            Set.of(Role.USER), UserStatus.ACTIVE, UserProfile.createDefault());
    }
}
```

---

## 6. Roadmap de Evolução

### v1.1 — Segurança Operacional (pré go-live)
- [ ] Rate limiting Redis (sliding window por IP + por e-mail) em `/login`, `/register`, `/refresh`
- [ ] Refresh token via HttpOnly cookie (ADR-010)
- [ ] Silent registration com e-mail diferenciado (S3)
- [ ] EmailPort real (SendGrid ou AWS SES)
- [ ] MFA via TOTP (Google Authenticator)

### v1.2 — Autenticação Social e Multi-tenant
- [ ] OAuth2 / Social Login (Google, GitHub)
- [ ] RBAC com permissões granulares (além de USER/ADMIN)
- [ ] Suporte a organizations/workspaces (multi-tenant)
- [ ] Passkeys / WebAuthn como alternativa à senha

### v2.0 — Extração de Microsserviço
- [ ] RS256 com JWKS endpoint (ADR-008)
- [ ] OAuth2 Authorization Server (Spring Authorization Server)
- [ ] SAML 2.0 para SSO corporativo
- [ ] Separação física do banco de dados IAM
- [ ] Audit log via Kafka (em vez de ApplicationEventPublisher)

---

## 7. Itens Pendentes / Débitos Técnicos

| ID | Descrição | Impacto | Versão |
|---|---|---|---|
| S3 | Silent registration | Segurança | v1.1 |
| S4 | HttpOnly cookie para refresh token | Segurança | v1.1 |
| S5 | Rate limiting Redis | Segurança | v1.1 |
| INC-1 | Domain events para block/activate | Observabilidade | v1.1 |
| PERF-1 | @EntityGraph em UserRepository | Performance | v1.1 |
| SEC-ADV-1 | Rotação automática de JWT secret | Segurança | v1.2 |

---

## Conclusão

O módulo IAM atende ao padrão arquitetural definido para o projeto. Todos os bloqueadores críticos foram resolvidos. Os itens remanescentes são débitos técnicos aceitos para o MVP com roadmap claro de resolução.

**Decisão: ✅ Módulo IAM aprovado. Prosseguir para `ai-core`.**
