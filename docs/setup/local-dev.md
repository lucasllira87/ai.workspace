# Guia de Desenvolvimento Local

## Pré-requisitos

| Ferramenta | Versão mínima | Verificar |
|-----------|--------------|-----------|
| Docker Desktop | 24.0 | `docker --version` |
| Docker Compose | v2.0 (plugin) | `docker compose version` |
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9 (ou use o wrapper) | `./mvnw -version` |
| Node.js | 20 | `node --version` |
| npm | 10 | `npm --version` |

---

## Variáveis de ambiente

```bash
cp .env.example .env
```

Edite `.env` com valores para o seu ambiente. Mínimo necessário para rodar localmente:

```env
POSTGRES_PASSWORD=qualquer_senha_forte
REDIS_PASSWORD=qualquer_senha_forte
JWT_SECRET=minimo_32_caracteres_aqui_para_hs256
```

> `.env` está no `.gitignore` e **nunca** deve ser commitado.

---

## Opção A — Infra no Docker, apps no host (recomendado para dev)

O `docker-compose.override.yml` usa `profiles: ["full"]` nas services `backend` e `frontend`, então `docker compose up` sobe **apenas** a infraestrutura.

### 1. Subir infra

```bash
docker compose up -d
# Sobe: postgres (5432) + redis (6379)
```

### 2. Backend

```bash
cd backend

# Primeira vez: deixe o Flyway rodar as migrações
./mvnw spring-boot:run

# Com perfil explícito
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

URLs disponíveis:
- API: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Métricas: `http://localhost:8080/actuator/prometheus`

### 3. Frontend

```bash
cd frontend
npm install          # apenas na primeira vez
npm run dev
```

URL disponível: `http://localhost:5173`

O Vite faz proxy de `/api` → `http://localhost:8080` automaticamente (`vite.config.ts`).

---

## Opção B — Stack completa em Docker

```bash
# Build e sobe tudo (infra + backend + frontend)
docker compose --profile full up --build

# Backend:  http://localhost:8080
# Frontend: http://localhost:3000
```

Para rebuild apenas do backend após mudanças:

```bash
docker compose --profile full up --build backend
```

---

## Opção C — Stack completa + Observabilidade

```bash
docker compose \
  -f docker-compose.yml \
  -f docker-compose.observability.yml \
  --profile full up --build

# Prometheus: http://localhost:9090
# Grafana:    http://localhost:3001  (usuário: admin / senha: admin)
```

O Grafana auto-provisiona o datasource Prometheus. Adicione dashboards em
`observability/grafana/provisioning/dashboards/` como arquivos `.json`.

---

## Comandos úteis

### Backend

```bash
# Compilar sem testes
./mvnw package -DskipTests

# Só testes unitários (sem Docker)
./mvnw test

# Testes unitários + integração (requer Docker para Testcontainers)
./mvnw verify

# Relatório de cobertura JaCoCo
./mvnw verify
# macOS: open target/site/jacoco/index.html
# Linux:  xdg-open target/site/jacoco/index.html
# Windows: start target/site/jacoco/index.html

# Rodar com debug na porta 5005
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

### Frontend

```bash
# Type-check
npm run type-check

# Lint
npm run lint

# Build de produção
npm run build

# Preview do build de produção localmente
npm run build && npm run preview
```

### Docker

```bash
# Ver logs do backend
docker compose logs -f backend

# Acessar psql no container
docker compose exec db psql -U aiworkspace -d aiworkspace

# Acessar redis-cli
docker compose exec redis redis-cli -a "$REDIS_PASSWORD"

# Parar tudo e remover volumes (reset completo)
docker compose down -v
```

---

## Variáveis de ambiente do backend

O backend lê variáveis de ambiente diretamente no `application.yml` via `${VAR:default}`.

| Variável | Default (dev) | Descrição |
|----------|--------------|-----------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/aiworkspace` | URL do PostgreSQL |
| `DATABASE_USERNAME` | `aiworkspace` | Usuário do banco |
| `DATABASE_PASSWORD` | `aiworkspace` | Senha do banco |
| `REDIS_HOST` | `localhost` | Host do Redis |
| `REDIS_PORT` | `6379` | Porta do Redis |
| `REDIS_PASSWORD` | *(vazio)* | Senha do Redis |
| `JWT_SECRET` | *(obrigatório — sem default)* | Chave HMAC-SHA256 ≥ 32 chars |
| `JWT_ACCESS_EXPIRATION` | `900` | Validade do access token (segundos) |
| `JWT_REFRESH_EXPIRATION` | `604800` | Validade do refresh token (segundos) |
| `OPENAI_API_KEY` | *(vazio)* | Chave da API OpenAI |
| `ANTHROPIC_API_KEY` | *(vazio)* | Chave da API Anthropic |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318/v1/traces` | Endpoint OTLP para tracing |

> Em produção, use `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` etc. para sobrescrever via compose.

---

## Flyway — Migrações

Migrações ficam em `backend/src/main/resources/db/migration/`.

Convenção de nomenclatura: `V<versão>__<descrição_com_underscores>.sql`

```
V1__create_users_table.sql
V2__create_subscriptions_table.sql
...
```

O Flyway roda automaticamente no startup. Para rodar manualmente:

```bash
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/aiworkspace \
                      -Dflyway.user=aiworkspace \
                      -Dflyway.password=<senha>
```

---

## Profiles do Spring

| Profile | Quando usar |
|---------|------------|
| *(nenhum / default)* | Desenvolvimento local com defaults |
| `test` | Testes automáticos (application-test.yml) |
| `prod` | Produção — JSON logging, sem Swagger exposto, OTLP ativo |

---

## Troubleshooting

### Porta 5432 já em uso

```bash
# Verificar quem está usando
sudo lsof -i :5432

# Ou mudar a porta no compose
# No docker-compose.yml: ports: ["5433:5432"]
# No DATABASE_URL: jdbc:postgresql://localhost:5433/aiworkspace
```

### Testcontainers falha no CI / WSL2

Certifique-se que o Docker está acessível:

```bash
docker info
# Deve retornar informações sem erro
```

Se estiver no WSL2, configure o Docker Desktop para integrar com o WSL2 distribution.

### Backend não conecta no Redis após restart do container

O Redis reinicia com um novo estado. Se o `REDIS_PASSWORD` mudou entre restarts, limpe o container:

```bash
docker compose down redis && docker compose up -d redis
```

### Frontend não encontra o backend (CORS)

O Vite faz proxy de `/api` para `localhost:8080` via `vite.config.ts`. Verifique se o backend está rodando:

```bash
curl http://localhost:8080/actuator/health
```

### JaCoCo falha com coverage abaixo de 80%

```bash
# Ver relatório detalhado
./mvnw verify
# macOS: open target/site/jacoco/index.html
# Linux:  xdg-open target/site/jacoco/index.html
# Windows: start target/site/jacoco/index.html
```

Verifique se novos serviços de domínio têm testes unitários correspondentes.
