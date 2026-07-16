# ADR-011 — Mitigação de User Enumeration e Timing Attacks

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

User enumeration é uma vulnerabilidade OWASP (A01, A07) onde um atacante consegue distinguir se um e-mail está cadastrado no sistema através de respostas diferentes (mensagens de erro distintas ou diferença de tempo de resposta).

Dois vetores identificados no módulo IAM:

### Vetor 1 — Timing Attack no Login
Quando o usuário não existe, o sistema retornava imediatamente (< 1ms). Quando existe mas senha errada, BCrypt rodava (~250ms). A diferença de tempo revela e-mails válidos mesmo sem ver o corpo da resposta.

### Vetor 2 — Mensagem de Erro no Registro
Endpoint `POST /register` lançava `ConflictException` com mensagem explícita "Email already in use" quando o e-mail já estava cadastrado. Um atacante pode usar este endpoint para enumerar e-mails válidos.

## Decisão

### Fix 1 — Constant-Time Login (implementado)

`LoginService` executa BCrypt mesmo quando o usuário não existe:

```java
private static final String DUMMY_HASH =
    "$2a$12$kZHhE8j3P4tGYqHd6J7b.OdSTwjLaqEbRSfJ4P2fkFG1k1j4OxTWm";

Optional<User> userOpt = userRepository.findByEmail(email);

if (userOpt.isEmpty()) {
    passwordHasher.matches(command.password(), HashedPassword.ofHash(DUMMY_HASH));
    throw new UnauthorizedException("Invalid credentials");
}

if (!passwordHasher.matches(command.password(), user.getHashedPassword())) {
    throw new UnauthorizedException("Invalid credentials");
}
```

A mesma mensagem genérica "Invalid credentials" é retornada independente de o e-mail existir ou não.

### Fix 2 — Registro com Resposta Genérica (roadmap v1.1)

Abordagem planejada para o endpoint de registro:

**Opção A (implementada no MVP):** Retornar HTTP 409 com mensagem genérica "If this email is not registered, you will receive a confirmation link." — não confirma nem nega que o e-mail já existe.

**Opção B (v1.1):** Adotar o padrão de "silent registration" — sempre retornar HTTP 200 e enviar e-mail de confirmação para o endereço fornecido, independente de estar cadastrado. Usuário já cadastrado recebe e-mail diferente ("sua conta já existe, tente fazer login"). A API não vaza a informação.

A Opção B requer EmailPort real implementado (SendGrid/SES), portanto é adiada para v1.1.

## Alternativas Consideradas

**Não fazer nada:** Inaceitável. Timing attack é confirmado e facilmente explorável com ferramentas como `curl --timing`.

**Rate limiting apenas:** Mitiga ataques automatizados mas não elimina o vazamento de informação. Complementar, não substituto.

**CAPTCHA no login:** Mitiga automação mas adiciona atrito ao usuário legítimo. Avaliar para v1.1 junto com rate limiting.

## Consequências

- Login tem tempo de resposta consistente independente de o usuário existir (~250ms pelo BCrypt)
- `DUMMY_HASH` é uma constante compilada — não é gerada em runtime, não adiciona latência de I/O
- A mensagem genérica "Invalid credentials" é usada para qualquer falha de autenticação (usuário não existe, senha errada, conta bloqueada) — simplifica lógica e elimina vazamento

## Notas Operacionais

- O `DUMMY_HASH` deve usar strength=12 (mesmo parâmetro que o BCrypt de produção) para garantir tempo equivalente. O hash hardcoded já está gerado com strength=12.
- Se a strength do BCrypt for aumentada futuramente, o `DUMMY_HASH` deve ser regenerado.
- Rate limiting (ADR futura) é camada complementar — implementar antes de go-live.
