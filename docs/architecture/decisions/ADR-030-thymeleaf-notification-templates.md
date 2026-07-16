# ADR-030: Thymeleaf para renderização de templates de email

**Status:** Accepted  
**Date:** 2026-07-15  
**Module:** notifications

---

## Contexto

O módulo precisa renderizar emails HTML com conteúdo dinâmico (invoiceId, courseTitle, etc.) a partir de templates. As opções principais são Thymeleaf, Freemarker, Mustache, StringTemplate ou geração manual de HTML.

## Decisão

Utilizar **Thymeleaf** como template engine para renderizar notificações, via `ThymeleafTemplateRendererAdapter` que implementa `TemplateRendererPort`.

## Alternativas avaliadas

| Opção | Prós | Contras |
|---|---|---|
| Thymeleaf | Auto-configure Spring Boot, XSS safe, cache built-in | Overhead de startup na primeira renderização |
| Freemarker | Rápido, maduro | Segunda dependência, sem auto-configure padrão |
| Mustache | Leve, logic-less | Expressividade limitada para layouts |
| HTML manual (StringBuilder) | Zero deps | Manutenção impossível, sem escape automático |
| Handlebars (Java) | Popular em frontend | Sem auto-configure Spring Boot |

## Justificativa

- Thymeleaf já é uma dependência do projeto (frontend/admin pages) — sem custo adicional
- Auto-configura com `spring-boot-starter-thymeleaf`
- Templates são HTML válido que pode ser aberto diretamente no browser para preview
- Escapa automaticamente variáveis em `th:text` — proteção XSS por padrão
- Cache de templates compilados habilitado por padrão em produção

## Trade-offs e riscos

- **Acoplamento a Thymeleaf no infrastructure layer:** `ThymeleafTemplateRendererAdapter` depende de `TemplateEngine`. O Port `TemplateRendererPort` isola o application/domain layers. Substituição futura por outra engine requer apenas novo adapter.
- **Templates HTML in `resources/templates/`:** Compartilham o diretório com possíveis templates de views MVC. Organização em subdiretório `notifications/` evita conflitos.
- **Startup:** 9 templates são compilados na primeira requisição. Pré-aquecimento opcional via `ApplicationRunner` em v1.1.

## Consequências

- `TemplateRendererPort` abstrai o mecanismo de renderização do application layer
- `ThymeleafTemplateRendererAdapter` mapeia `NotificationType` → nome do template
- Templates em `resources/templates/notifications/*.html`
- `Context` do Thymeleaf recebe `Map<String, Object> variables` do command
- `Timer("notifications.template.render")` mede latência de renderização

## Segurança

- `th:text` auto-escapa — nenhum template usa `th:utext`
- Variáveis de eventos (IDs, títulos) não contêm HTML injetável
- Thymeleaf rejeita templates com sintaxe inválida em tempo de compilação
