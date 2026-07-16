# AI Workspace — Modelo de Domínio

**Versão:** 1.0  
**Data:** 2026-07-14  
**Status:** Aprovado

---

## Princípio Fundamental

A camada de domínio não conhece PostgreSQL, Redis, OpenAI, pgvector ou qualquer tecnologia externa.  
Todo acesso externo ocorre exclusivamente via **Ports** (interfaces) implementadas na camada de Infrastructure.

---

## Bounded Contexts

### Identity & Access
- **Aggregate:** `User` → entidades: `UserProfile`, `Device` → value objects: `Email`, `HashedPassword`, `FullName`, `UserId`, `Role`
- **Entity:** `RefreshToken` (rotativo, revogável, por dispositivo)
- **Domain Events:** `UserRegistered`, `UserActivated`, `PasswordChanged`

### Document Intelligence
- **Aggregate:** `Document` → entidades: `DocumentChunk` (com `DocumentChunkMetadata`) → value objects: `DocumentId`, `StorageReference`, `MimeType`, `DocumentTitle`
- **Aggregate:** `Conversation` → entidades: `Message` → value objects: `ConversationId`, `MessageContent`, `MessageRole`
- **Domain Events:** `DocumentUploaded`, `DocumentIndexed`, `DocumentIndexingFailed`, `ConversationStarted`, `MessageAdded`

### Learning
- **Aggregate:** `StudyMaterial` → entidades: `Summary`, `Question`, `Flashcard`
- **Domain Events:** `StudyMaterialUploaded`, `SummaryGenerated`, `QuestionsGenerated`, `FlashcardsGenerated`

### AI Core
- **Value Objects (stateless):** `AIRequest`, `AIResponse`, `AIMessage`, `EmbeddingRequest`, `EmbeddingResult`

### Audit
- **Entity:** `AuditEvent` (append-only, imutável após criação)

---

## Ports de Saída (Infrastructure Interfaces)

### Document Intelligence
```
VectorSearchPort
├── storeEmbeddings(documentId, List<ChunkEmbedding>)
├── searchSimilar(queryEmbedding, topK, documentId?) → List<ScoredChunk>
└── deleteByDocumentId(documentId)

StoragePort
├── store(file, path) → StorageReference
├── retrieve(reference) → InputStream
└── delete(reference)

ChunkingStrategy                     ← plugável por formato
├── List<DocumentChunk> chunk(content, metadata)

EmbeddingPort                        ← provider-agnostic
└── List<float[]> embed(List<String> texts) → EmbeddingResult

IngestionPort                        ← sync agora, async no futuro
└── scheduleIngestion(documentId)
```

### AI Core
```
AIProviderPort
├── chat(AIRequest) → AIResponse
├── summarize(content) → String
├── generateQuestions(content) → List<Question>
└── generateFlashcards(content) → List<Flashcard>
```

---

## DocumentChunkMetadata (Value Object)

```
documentId:      DocumentId
pageNumber:      Integer?
chunkOrder:      Integer
sectionTitle:    String?
documentType:    DocumentType  (PDF, MARKDOWN, HTML, DOCX)
detectedLanguage: String       (ISO 639-1: "pt", "en"...)
indexedAt:       Instant
documentVersion: Integer
```

---

## Pipeline de Ingestão (async-ready)

```
1. Upload → DocumentUploaded event publicado
2. IngestionPort.scheduleIngestion(documentId)
   ├── [MVP] SynchronousIngestionAdapter executa inline
   └── [Futuro] AsyncIngestionAdapter enfileira em Redis/RabbitMQ
3. ChunkingStrategy.chunk(content, metadata)
4. EmbeddingPort.embed(chunks)
5. VectorSearchPort.storeEmbeddings(documentId, embeddings)
6. Document.markAsIndexed()
7. IngestionMetrics registrado
8. DocumentIndexed event publicado
```

---

## Métricas de Ingestão (IngestionMetrics — Value Object)

```
documentId:        DocumentId
processingTimeMs:  Long
pageCount:         Integer
chunkCount:        Integer
totalTokens:       Integer
embeddingCostUsd:  BigDecimal
indexingTimeMs:    Long
embeddingProvider: String
embeddingModel:    String
status:            IngestionStatus
errorMessage:      String?
startedAt:         Instant
completedAt:       Instant?
```
