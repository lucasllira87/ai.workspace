# Como rodar o AI Workspace

Três modos de execução: infra no Docker + apps no host (recomendado para dev), stack completa em Docker, ou stack completa com observabilidade.

---

## Pré-requisitos

| Ferramenta | Versão mínima | Verificar |
|-----------|--------------|-----------|
| Docker Desktop | 24.0 | `docker --version` |
| Docker Compose | v2.0 (plugin) | `docker compose version` |
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9 (ou use o wrapper `./mvnw`) | `./mvnw -version` |
| Node.js | 20 | `node --version` |
| npm | 10 | `npm --version` |

---

## 1. Configurar variáveis de ambiente

```bash
cp .env.example .env
```

Edite `.env` com os valores para o seu ambiente. Mínimo obrigatório:

```env
POSTGRES_PASSWORD=qualquer_senha_forte
REDIS_PASSWORD=qualquer_senha_forte
JWT_SECRET=minimo_32_caracteres_aqui_para_hs256_algorithm
```

> **Nota:** `JWT_SECRET` é obrigatório e não tem valor default — a aplicação falha na startup se não estiver definido.  
> `.env` está no `.gitignore` e nunca deve ser commitado.

Para usar AI providers, adicione ao `.env`:

```env
OPENAI_API_KEY=sk-...
OPENAI_ENABLED=true

# Ou Anthropic:
ANTHROPIC_API_KEY=sk-ant-...
ANTHROPIC_ENABLED=true

# Ou Ollama local (sem chave):
OLLAMA_ENABLED=true
OLLAMA_BASE_URL=http://localhost:11434
```

---

## Modo A — Infra no Docker, apps no host (recomendado)

Ideal para desenvolvimento: hot reload nos dois lados, sem rebuild de imagem.

### 1. Subir infraestrutura

```bash
docker compose up -d
# Sobe: postgres (5432) + redis (6379)
```

### 2. Rodar o backend

```bash
cd backend
./mvnw spring-boot:run
```

| Endpoint | URL |
|---------|-----|
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |
| Métricas | http://localhost:8080/actuator/prometheus |

### 3. Rodar o frontend

```bash
cd frontend
npm install        # apenas na primeira vez
npm run dev
```

Frontend disponível em **http://localhost:5173**  
O Vite faz proxy de `/api` → `http://localhost:8080` automaticamente.

---

## Modo B — Stack completa em Docker

```bash
docker compose --profile full up --build
```

| Serviço | URL |
|---------|-----|
| Backend | http://localhost:8080 |
| Frontend | http://localhost:3000 |

Para rebuild apenas do backend após mudanças:

```bash
docker compose --profile full up --build backend
```

---

## Modo C — Stack completa + Observabilidade

```bash
docker compose \
  -f docker-compose.yml \
  -f docker-compose.observability.yml \
  --profile full up --build
```

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| Backend | http://localhost:8080 | — |
| Frontend | http://localhost:3000 | — |
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3001 | admin / admin |

O Grafana provisiona o datasource Prometheus automaticamente.

---

## Testes

### Testes unitários (sem Docker)

```bash
cd backend
./mvnw test
```

Execução rápida, sem dependências externas. Cobre domain model, application services e adapters com mocks.

### Testes unitários + integração

```bash
cd backend
./mvnw verify
```

Requer Docker (Testcontainers sobe PostgreSQL + Redis automaticamente). Inclui:
- Testes de integração com banco real
- `UserRegistrationFlowIT` — flow completo: registro → login → trial subscription → audit event
- ArchUnit — 6 regras arquiteturais verificadas

### Relatório de cobertura (JaCoCo — threshold 80%)

```bash
cd backend
./mvnw verify

# Abrir relatório:
# macOS:   open target/site/jacoco/index.html
# Linux:   xdg-open target/site/jacoco/index.html
# Windows: start target/site/jacoco/index.html
```

### Frontend

```bash
cd frontend
npm run type-check   # TypeScript strict
npm run lint         # ESLint
npm run build        # Build de produção
```

---

## Variáveis de ambiente completas

| Variável | Default | Descrição |
|----------|---------|-----------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/aiworkspace` | URL do PostgreSQL |
| `DATABASE_USERNAME` | `aiworkspace` | Usuário do banco |
| `DATABASE_PASSWORD` | `aiworkspace` | Senha do banco |
| `REDIS_HOST` | `localhost` | Host do Redis |
| `REDIS_PORT` | `6379` | Porta do Redis |
| `REDIS_PASSWORD` | *(vazio)* | Senha do Redis |
| `JWT_SECRET` | *(obrigatório — sem default)* | Chave HMAC-SHA256 ≥ 32 chars |
| `JWT_ACCESS_EXPIRATION` | `900` | Validade do access token (segundos) |
| `JWT_REFRESH_EXPIRATION` | `604800` | Validade do refresh token (segundos) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:3000` | Origens permitidas pelo CORS |
| `OPENAI_API_KEY` | *(vazio)* | Chave da API OpenAI |
| `OPENAI_ENABLED` | `false` | Habilita provider OpenAI |
| `ANTHROPIC_API_KEY` | *(vazio)* | Chave da API Anthropic |
| `ANTHROPIC_ENABLED` | `false` | Habilita provider Anthropic |
| `GOOGLE_AI_API_KEY` | *(vazio)* | Chave da API Google AI |
| `GOOGLE_AI_ENABLED` | `false` | Habilita provider Google AI |
| `OLLAMA_ENABLED` | `false` | Habilita Ollama local |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Endpoint do Ollama |
| `AI_MAX_COST_PER_REQUEST` | `0.10` | Limite de custo estimado por request (USD) |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318/v1/traces` | Endpoint OTLP para tracing |
| `STRIPE_SECRET_KEY` | *(vazio)* | Chave da API Stripe (billing) |
| `STRIPE_WEBHOOK_SECRET` | *(vazio)* | Secret de webhook do Stripe |
| `MAIL_HOST` | `smtp.mailgun.org` | Host SMTP para e-mails |
| `MAIL_PORT` | `587` | Porta SMTP (STARTTLS) |
| `MAIL_USERNAME` | *(vazio)* | Usuário SMTP |
| `MAIL_PASSWORD` | *(vazio)* | Senha SMTP |

---

## Comandos úteis

```bash
# Ver logs do backend
docker compose logs -f backend

# Acessar psql no container
docker compose exec db psql -U aiworkspace -d aiworkspace

# Acessar redis-cli
docker compose exec redis redis-cli -a "$REDIS_PASSWORD"

# Rodar backend com debug na porta 5005
cd backend
./mvnw spring-boot:run \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Reset completo (para e remove volumes)
docker compose down -v
```

---

## Troubleshooting

### Porta 5432 já em uso

```bash
# Ver o processo
sudo lsof -i :5432   # Linux/macOS
netstat -ano | findstr :5432   # Windows

# Ou mudar a porta no compose e no DATABASE_URL
```

### Backend não sobe com erro de JWT_SECRET

A variável `JWT_SECRET` é obrigatória. Adicione ao `.env`:

```env
JWT_SECRET=qualquer-string-com-pelo-menos-32-caracteres-aqui
```

### Testcontainers falha no CI / WSL2

```bash
docker info   # deve retornar sem erro
```

No WSL2, configure o Docker Desktop para integrar com a distribution.

### Frontend não encontra o backend

O Vite faz proxy de `/api` → `localhost:8080`. Verifique se o backend está rodando:

```bash
curl http://localhost:8080/actuator/health
```

### JaCoCo falha com coverage abaixo de 80%

Novos serviços de domínio precisam de testes unitários. Veja o relatório:

```bash
./mvnw verify
# macOS:   open target/site/jacoco/index.html
# Linux:   xdg-open target/site/jacoco/index.html
# Windows: start target/site/jacoco/index.html
```
