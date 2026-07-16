# ADR-005 — Redis no MVP

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

O Redis não é estritamente necessário para o volume do MVP (100–1.000 usuários). Contudo, sua inclusão desde o início agrega valor técnico ao projeto, simplifica a implementação de algumas features e prepara a arquitetura para crescimento.

## Decisão

Redis 7 incluído no MVP com os seguintes usos:

| Uso | Chave (padrão) | TTL |
|---|---|---|
| Cache de respostas de IA | `ai:response:{hash_do_prompt}` | 1 hora |
| Cache de perfil de usuário | `user:profile:{user_id}` | 15 minutos |
| Cache de roles/permissões | `user:roles:{user_id}` | 15 minutos |
| Rate limiting por IP | `rate:ip:{ip}:{endpoint}` | 1 minuto (sliding) |
| Rate limiting por usuário | `rate:user:{user_id}:{endpoint}` | 1 minuto (sliding) |
| Tokens de reset de senha | `auth:reset:{token}` | 30 minutos |
| Tokens de verificação de e-mail | `auth:verify:{token}` | 24 horas |

**Não usaremos Redis como message broker no MVP.** Para mensageria futura, avaliaremos Redis Streams vs. RabbitMQ vs. Kafka conforme o volume.

## Consequências

**Positivas:**
- Cache de respostas de IA reduz custos de API em cenários com prompts repetidos
- Rate limiting sem estado no BD da aplicação
- Tokens temporários (reset de senha) sem poluir o PostgreSQL
- Preparação para sessões distribuídas quando extrairmos módulos

**Negativas / Riscos:**
- Mais um serviço para operar em produção
- Dados voláteis: perda do Redis não quebra o sistema (todos os usos são cache/temporários)
- Requer configuração de persistência (AOF) se quisermos durabilidade dos tokens de reset
