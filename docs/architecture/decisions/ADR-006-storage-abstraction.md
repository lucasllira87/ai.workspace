# ADR-006 — Abstração de Storage de Arquivos

**Data:** 2026-07-14  
**Status:** Aceito  
**Decisores:** Lucas (Engenheiro), Claude (Tech Lead / Arquiteto)

---

## Contexto

O sistema precisa armazenar arquivos enviados pelos usuários (PDFs, materiais de estudo). No MVP, o volume é pequeno. No futuro, migraremos para storage em nuvem (S3 ou compatível).

## Decisão

Porta de saída `StoragePort` definida na camada de aplicação. Duas implementações:

```
StoragePort (interface)
├── StorageReference store(MultipartFile file, String path)
├── InputStream retrieve(StorageReference reference)
├── void delete(StorageReference reference)
└── String getPublicUrl(StorageReference reference)

Implementações:
├── LocalFileStorageAdapter   ← Ativo no MVP / desenvolvimento
└── S3StorageAdapter          ← Produção futura (AWS S3 / Cloudflare R2)
```

`StorageReference` é um Value Object contendo o caminho lógico do arquivo — independente do storage físico.

Implementação ativa definida via `application.properties`:
```
storage.provider=local
storage.local.base-path=/data/uploads
```

## Consequências

- Migração local → S3 é troca de configuração + implementação do Adapter; zero alteração nos use cases
- LocalFileStorageAdapter adequado para desenvolvimento e portfólio
- Em produção real: S3StorageAdapter com presigned URLs para acesso direto do cliente
