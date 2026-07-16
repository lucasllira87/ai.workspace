# ADR-021: Emissão de certificado via @TransactionalEventListener síncrono

**Status:** Accepted  
**Data:** 2026-07-15  
**Módulo:** learning

## Contexto

Quando um aluno completa todas as lessons de um curso (`CourseCompletedEvent`), o sistema deve emitir um certificado. O `CertificateService` precisa ouvir esse evento e persistir o certificado. A decisão é: usar `@Async` (processamento assíncrono em thread pool separado) ou processamento síncrono.

## Decisão

`CertificateService.onCourseCompleted()` usa `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Transactional` sem `@Async`.

## Justificativa

- Emissão de certificado é uma operação rápida — um único `INSERT` na tabela `learning.certificates`
- `@Async` adiciona complexidade (thread pool, tratamento de falha, retry) sem benefício de latência perceptível
- `@TransactionalEventListener(AFTER_COMMIT)` garante que o certificado só é criado se o `completeLesson` foi commitado com sucesso
- A nova `@Transactional` no listener abre uma nova transação separada — o certificado tem seu próprio ACID boundary
- Em caso de falha na emissão, o erro é propagado para o caller (completeLesson) que pode ser retryado pelo cliente

## Consequências

- Se a emissão do certificado falhar, o enrollment já está COMPLETED mas sem certificado. Isso é aceitável porque a operação de completeLesson retorna erro ao cliente, que pode retryar. Um job de reconciliação pode verificar enrollments COMPLETED sem certificado periodicamente.
- Contraste com `DocumentIndexingListener` (documentos) que usa `@Async` porque indexação no vector store pode levar segundos/minutos.
