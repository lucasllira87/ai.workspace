# ADR-002 — Clean Architecture + DDD por Módulo

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

Cada módulo do Monólito Modular precisa de uma organização interna que:
- Isole a regra de negócio de frameworks e infraestrutura
- Permita testar a lógica de negócio sem Spring, banco de dados ou rede
- Facilite a troca de implementações (banco, IA, storage) sem alterar domínio
- Expresse a linguagem do domínio de forma explícita no código

## Decisão

Adotamos **Clean Architecture** (Ports & Adapters / Hexagonal) combinada com **Domain-Driven Design (DDD)** dentro de cada módulo.

### Regra de Dependência
As dependências sempre apontam de fora para dentro:

```
Presentation → Application → Domain ← Infrastructure
```

O domínio não conhece nenhuma camada externa. A infraestrutura implementa interfaces definidas pelo domínio.

### Camadas

**Domain:** Entidades, Value Objects, Aggregates, Domain Services, Domain Events, interfaces de repositório. Zero dependência de Spring ou qualquer framework.

**Application:** Casos de uso (orquestração), portas de entrada (in), portas de saída (out), DTOs. Conhece apenas o domínio.

**Infrastructure:** Implementações concretas das portas de saída — repositórios JPA, adaptadores de IA, de storage, de e-mail. Conhece Spring, JPA, Redis, etc.

**Presentation:** Controllers REST, request/response bodies, mappers. Converte HTTP para DTOs da camada de aplicação.

## Consequências

**Positivas:**
- Domínio 100% testável sem Spring context
- Troca de qualquer implementação externa sem alterar regra de negócio
- Código expressa linguagem do domínio explicitamente
- Facilita onboarding: cada camada tem responsabilidade única e clara

**Negativas / Riscos:**
- Mais classes e mapeamentos que uma arquitetura em camadas tradicional
- Curva de aprendizado inicial maior
- Risco de "Clean Architecture theater" — seguir a forma sem o conteúdo

## Mitigações

- Mapeamento de entidades ocorre apenas nas camadas de infraestrutura e apresentação
- Use cases com nomes explícitos no infinitivo: `UploadDocumentUseCase`, `GenerateSummaryUseCase`
- Value Objects para conceitos com regra de negócio: `Email`, `DocumentContent`, `AIPrompt`
