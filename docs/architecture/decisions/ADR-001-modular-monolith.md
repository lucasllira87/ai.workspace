# ADR-001 — Monólito Modular como Estilo Arquitetural Inicial

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

O AI Workspace é uma plataforma SaaS em fase de MVP com volume esperado de 100–1.000 usuários ativos e até 10.000 requisições por dia. O projeto tem objetivo de portfólio e aprendizado, com foco em qualidade arquitetural e evolução sustentável.

A decisão sobre o estilo arquitetural inicial é a mais impactante do projeto.

## Decisão

Adotamos **Monólito Modular** como estilo arquitetural inicial.

O sistema será deployado como uma única unidade executável, mas internamente organizado em módulos com fronteiras explícitas de domínio. Módulos não acessam diretamente pacotes internos uns dos outros — a comunicação ocorre via interfaces públicas (portas de aplicação) ou eventos de domínio.

## Alternativas Consideradas

### Opção A — Microsserviços desde o início
- **Prós:** Escalabilidade independente por serviço; isolamento de falhas; deploy independente
- **Contras:** Overhead operacional massivo para MVP; complexidade de rede, observabilidade e autenticação distribuída; custo de infra muito maior; sem justificativa de negócio no volume atual
- **Decisão:** Rejeitado

### Opção B — Monólito Clássico (sem modularização)
- **Prós:** Máxima simplicidade inicial
- **Contras:** Acumulação de dívida técnica; dificuldade de extração futura; ausência de fronteiras de domínio; inviabiliza o objetivo do portfólio
- **Decisão:** Rejeitado

### Opção C — Monólito Modular ✅
- **Prós:** Sem overhead de microsserviços; fronteiras de domínio preservadas; extração futura viável; complexidade proporcional ao volume; adequado para portfólio técnico
- **Contras:** Exige disciplina para manter fronteiras; risco de acoplamento se as regras não forem seguidas
- **Decisão:** Aceito

## Consequências

**Positivas:**
- Deploy e operação simples no MVP
- Fronteiras claras permitem extração de módulos como microsserviços no futuro
- Documentar essa evolução nos ADRs agrega valor ao portfólio
- Facilita onboarding e entendimento do sistema

**Negativas / Riscos:**
- Requer disciplina de arquitetura para não violar fronteiras entre módulos
- Banco de dados compartilhado pode se tornar gargalo no futuro (mitigado por schemas separados por domínio)

## Critérios para Extrair um Módulo como Microsserviço

Um módulo deve ser extraído quando:
1. Tiver gargalo de performance isolado que não pode ser resolvido com cache/otimização local
2. Requerer deploy independente por razão de negócio
3. Houver equipe dedicada para operá-lo
4. A carga justificar escalabilidade horizontal independente
