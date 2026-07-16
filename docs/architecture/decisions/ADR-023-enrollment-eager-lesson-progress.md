# ADR-023: Enrollment inicializa LessonProgress para todas as lessons no momento do enroll

**Status:** Accepted  
**Data:** 2026-07-15  
**Módulo:** learning

## Contexto

Quando um aluno faz enroll em um curso, o `Enrollment` aggregate precisa rastrear o progresso de cada lesson. A alternativa é criar os registros de `LessonProgress` lazy (apenas quando o aluno acessar a lesson pela primeira vez) ou eager (no momento do enroll).

## Decisão

`Enrollment.enroll()` inicializa `LessonProgress.notStarted()` para todas as lessons do curso no momento do enroll.

## Justificativa

- O aggregate `Enrollment` é a única fonte de verdade para estado de progresso — ele precisa saber quais lessons existem para validar `startLesson()` e `completeLesson()`
- `allLessonsCompleted()` verifica se **todas** as lessons estão COMPLETED — impossível sem o mapa completo
- Lazy init quebraria a invariante de auto-completion: se o aluno completa a última lesson antes de "inicializar" as outras, o aggregate não teria como saber que todas estão completas
- O número de lessons por curso é tipicamente pequeno (< 50) — custo de storage negligível

## Consequências

- Se lessons forem adicionadas ao curso após o enroll, o Enrollment existente **não** incluirá as novas lessons automaticamente. O enroll é um snapshot das lessons no momento da matrícula — alinhado com a semântica de cursos com conteúdo versionado.
- Ao serializar para JPA, cada `LessonProgress` vira uma row em `learning.lesson_progress` com `UUID.randomUUID()` como PK — idempotência de save é garantida pelo orphanRemoval=true + CASCADE.
