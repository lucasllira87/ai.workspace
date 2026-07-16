# ADR-016: Indexação Assíncrona via Spring Events + @TransactionalEventListener(AFTER_COMMIT)

**Status:** Aceito  
**Data:** 2026-07-15  
**Módulo:** documents

---

## Contexto

O upload de documentos deve retornar ao cliente (202 Accepted) antes que a indexação vetorial seja concluída. A indexação envolve leitura de arquivo, parsing, chunking, geração de embeddings e escrita no pgvector — operações que podem levar de 5 a 60 segundos dependendo do tamanho e do provider de IA.

O desafio é garantir que a indexação só comece **após o commit** da transação de upload. Se o evento disparar antes do commit e o commit falhar, o listener tentaria indexar um documento que não existe no banco.

---

## Decisão

Usar `ApplicationEventPublisher` + `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async("documentIndexingExecutor")`.

O `Document.upload()` registra o `DocumentUploadedEvent` no aggregate. Após `documentRepository.save()`, os eventos são publicados via `ApplicationEventPublisher`. O Spring só entrega o evento ao listener após o commit da transação de upload.

O listener executa em thread pool dedicado (`documentIndexingExecutor`: core=4, max=8, queue=50).

---

## Alternativas Consideradas

### Mensageria (Kafka / RabbitMQ)

**Vantagens:** durável, replay, escalável para workers externos.  
**Desvantagens:** adiciona infraestrutura externa ao MVP; complexidade de idempotência e dead-letter queue; transactional outbox necessário para garantia exata-once.  
**Descartado:** overengineering para MVP; adotável em v2.0 quando documents for extraído para microsserviço.

### Thread separada com `CompletableFuture`

**Vantagens:** simples.  
**Desvantagens:** sem garantia de AFTER_COMMIT; race condition real — a thread poderia tentar ler o documento antes do commit.  
**Descartado:** unsafe.

### Polling periódico (scheduled job)

**Vantagens:** tolerante a falhas, re-tentável.  
**Desvantagens:** latência de indexação determinada pelo intervalo do scheduler (mínimo ~1s); desperdiça recursos quando fila vazia.  
**Descartado:** experiência de usuário inferior; viable como fallback/recovery para v1.1.

---

## Consequências

**Positivas:**
- Zero infraestrutura adicional — funciona com Spring e PostgreSQL
- Garantia de commit-first — sem zombie indexing
- Thread pool bounded — sem risco de OOM sob carga
- Graceful shutdown — indexações em andamento completam antes do processo parar

**Negativas / Trade-offs:**
- Não durável — se o processo morrer após o commit mas antes do evento ser entregue, o documento fica em status UPLOADED para sempre. Mitigação: scheduled job de recovery (v1.1) que re-dispara indexação para documentos em UPLOADED/INDEXING há mais de N minutos.
- In-memory queue — `queueCapacity=50`; sob burst acima de 50 uploads simultâneos, novos uploads são aceitos (202) mas indexação é enfileirada no DB como UPLOADED até slot disponível.
