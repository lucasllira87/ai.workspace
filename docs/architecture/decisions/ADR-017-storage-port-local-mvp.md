# ADR-017: StoragePort Abstrato com LocalFileStorageAdapter no MVP

**Status:** Aceito  
**Data:** 2026-07-15  
**Módulo:** documents

---

## Contexto

O módulo documents precisa persistir arquivos binários (PDFs, DOCX, Markdown) de forma independente do banco de dados relacional. A decisão de storage — filesystem local, S3, GCS, Azure Blob — deve ser isolável para permitir migração sem afetar o domínio ou a camada de aplicação.

A plataforma é MVP rodando em uma única máquina (Docker Compose). Cloud storage é requisito de v1.2+ quando multi-tenant e alta disponibilidade se tornarem necessários.

---

## Decisão

Definir `StoragePort` na camada de aplicação (`documents/application/port/out/`):

```java
public interface StoragePort {
    String store(UUID documentId, String ownerId, byte[] content, String fileName);
    byte[] retrieve(String storagePath);
    void delete(String storagePath);
}
```

Implementar `LocalFileStorageAdapter` para MVP: armazena arquivos em `${documents.storage.local-base-path}/{ownerId}/{documentId}_{sanitizedFileName}`.

Sanitização de nome via regex `[^a-zA-Z0-9._-]` → `_` para prevenir path traversal.

---

## Alternativas Consideradas

### AWS S3 desde o início

**Vantagens:** escalável, durável, sem gestão de disco.  
**Desvantagens:** custo e complexidade de infra no MVP; requer localstack para dev local; adiciona latência de rede para operações de arquivo.  
**Descartado para MVP:** `StoragePort` garante migração transparente quando necessário.

### Armazenar arquivo no PostgreSQL (bytea / Large Object)

**Vantagens:** atomicidade com metadados; sem infra adicional.  
**Desvantagens:** degrada performance do PostgreSQL para arquivos grandes; backup e restore mais lentos; impede CDN/presigned URL direto.  
**Descartado:** anti-padrão para arquivos binários de tamanho arbitrário.

---

## Consequências

**Positivas:**
- Troca de implementação (local → S3) sem alterar nenhuma linha da camada de domínio ou aplicação
- Dev local sem dependências de cloud
- `LocalFileStorageAdapter` é simples e auditável (< 80 linhas)

**Negativas / Trade-offs:**
- Não escalável horizontalmente — múltiplas instâncias da aplicação precisam de volume compartilhado (NFS) ou migração para S3
- `byte[]` completo em memória: 50 MB por documento por operação. Para v1.1: mudar `store()` e `retrieve()` para `InputStream`-based

**Plano de migração (v1.2):**
Implementar `S3StorageAdapter` que implementa `StoragePort`. Ativar via `@ConditionalOnProperty("documents.storage.provider=s3")`. Zero alteração no domínio ou aplicação.
