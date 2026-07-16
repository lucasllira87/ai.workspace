# ADR-019: Comunicação Cross-Module via Portas Dedicadas (documents → ai-core)

**Status:** Aceito  
**Data:** 2026-07-15  
**Módulos:** documents, ai-core

---

## Contexto

O módulo `documents` precisa de duas capacidades do módulo `ai-core`:
1. **Ingestion:** parsear arquivo → chunkar → embeddar → gravar no pgvector
2. **Query:** embeddar pergunta → busca vetorial → resposta LLM

A questão arquitetural é: como o módulo `documents` acessa essas capacidades sem criar acoplamento direto entre módulos?

Em um monólito modular, o acoplamento direto (import de classes internas de outro módulo) é o maior risco à evolução independente de cada módulo.

---

## Decisão

O módulo `documents` define suas próprias portas de saída na sua **própria** camada de aplicação:

```
documents/application/port/out/AIIngestionPort.java
documents/application/port/out/AIQueryPort.java
```

Os adapters em `documents/infrastructure/aicore/` implementam essas portas e delegam para beans do ai-core:

```
AICoreIngestionAdapter → IngestDocumentUseCase (ai-core application)
AICoreQueryAdapter     → GenerateEmbeddingUseCase + VectorStore + ChatUseCase (ai-core application)
```

**Regra:** nenhum tipo de `com.aiworkspace.aicore.*` é importado na camada de domínio ou aplicação do módulo `documents`. O acoplamento existe **apenas** em `documents/infrastructure/aicore/`.

---

## Alternativas Consideradas

### Import direto do ai-core na camada de aplicação do documents

```java
// NO documents/application/service/DocumentChatService.java
import com.aiworkspace.aicore.application.port.in.ChatUseCase;
```

**Vantagens:** menos código (sem adapter).  
**Desvantagens:** acoplamento direto — qualquer refatoração no ai-core quebra o documents; testes de documents requerem o contexto completo do ai-core; impede extração de módulos para microsserviços independentes.  
**Descartado:** viola o princípio fundamental do monólito modular.

### Shared kernel com interfaces comuns

Definir `AICapabilityPort` em um módulo `shared-ai` que ambos os módulos usam.

**Vantagens:** reutilizável por múltiplos módulos futuros.  
**Desvantagens:** shared kernel vira gargalo de evolução — qualquer mudança na interface afeta todos os consumidores; os módulos futuros (learning, code-review) podem ter necessidades diferentes.  
**Descartado para MVP:** introduzir shared kernel quando 3+ módulos precisarem das mesmas capacidades.

### Event-driven (documentos publicam eventos, ai-core consome)

**Vantagens:** acoplamento zero em tempo de compilação.  
**Desvantagens:** requer mensageria; rastreabilidade de erros mais complexa; resultado de ingestion precisa ser comunicado de volta (reply pattern).  
**Descartado:** overengineering para MVP; plano para v2.0 com Kafka.

---

## Consequências

**Positivas:**
- Domínio e aplicação do `documents` testáveis sem ai-core no contexto
- ai-core pode refatorar suas interfaces internas sem afetar `documents/application`
- Plano de extração para microsserviço: substituir `AICoreIngestionAdapter` por `HttpIngestionAdapter` (REST call) sem alterar nada acima da infrastructure layer
- Padrão aplicado consistentemente com outros módulos (IAM usa `TokenPolicyPort`, `PasswordHasher`)

**Negativas / Trade-offs:**
- Mais arquivos (2 portas + 2 adapters vs. 0 se acoplamento direto)
- Mapeamento manual entre tipos do ai-core e os records das portas do documents (`IngestionResultDto` → `IngestionResult`, `ChunkSearchResult` → `ChunkSource`)
- Custo de manutenção do mapping deve ser considerado se as interfaces do ai-core mudarem frequentemente

**Regra de verificação:**
```bash
# Nenhuma linha deve existir com este padrão na camada de aplicação do documents:
grep -r "com.aiworkspace.aicore" \
  backend/src/main/java/com/aiworkspace/documents/domain/ \
  backend/src/main/java/com/aiworkspace/documents/application/
```
