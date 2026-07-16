package com.aiworkspace.documents.application.service;

import com.aiworkspace.documents.application.command.ChatWithDocumentCommand;
import com.aiworkspace.documents.application.dto.ChatWithDocumentDto;
import com.aiworkspace.documents.application.dto.ChunkSourceDto;
import com.aiworkspace.documents.application.port.in.ChatWithDocumentUseCase;
import com.aiworkspace.documents.application.port.out.AIQueryPort;
import com.aiworkspace.documents.application.port.out.DocumentConfigPort;
import com.aiworkspace.documents.application.port.out.DocumentRepository;
import com.aiworkspace.documents.domain.exception.DocumentNotFoundException;
import com.aiworkspace.documents.domain.exception.DocumentNotIndexedException;
import com.aiworkspace.documents.domain.model.Document;
import com.aiworkspace.documents.domain.model.DocumentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentChatService implements ChatWithDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final AIQueryPort aiQueryPort;
    private final DocumentConfigPort config;

    public DocumentChatService(DocumentRepository documentRepository,
                                AIQueryPort aiQueryPort,
                                DocumentConfigPort config) {
        this.documentRepository = documentRepository;
        this.aiQueryPort = aiQueryPort;
        this.config = config;
    }

    @Override
    @Transactional(readOnly = true)
    public ChatWithDocumentDto chat(ChatWithDocumentCommand command) {
        Document document = documentRepository.findByIdAndOwnerId(
                command.documentId(), command.ownerId())
                .orElseThrow(() -> new DocumentNotFoundException(command.documentId()));

        if (document.getStatus() != DocumentStatus.INDEXED) {
            throw new DocumentNotIndexedException(command.documentId(), document.getStatus());
        }

        List<AIQueryPort.ChunkSource> sources = aiQueryPort.findRelevantChunks(
                command.documentId(), command.question(), config.getRagTopK(), config.getRagMinSimilarity());

        if (sources.isEmpty()) {
            return ChatWithDocumentDto.noContext(
                    "Não encontrei informações relevantes no documento para responder essa pergunta.");
        }

        String context = sources.stream()
                .map(AIQueryPort.ChunkSource::content)
                .collect(Collectors.joining("\n\n---\n\n"));

        String answer = aiQueryPort.chatWithContext(
                command.question(), context, document.getTitle());

        List<ChunkSourceDto> sourceDtos = sources.stream()
                .map(ChunkSourceDto::from)
                .toList();

        return ChatWithDocumentDto.withSources(answer, sourceDtos);
    }
}
