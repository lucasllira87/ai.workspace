package com.aiworkspace.documents.presentation;

import com.aiworkspace.documents.application.command.ChatWithDocumentCommand;
import com.aiworkspace.documents.application.dto.ChatWithDocumentDto;
import com.aiworkspace.documents.application.port.in.ChatWithDocumentUseCase;
import com.aiworkspace.documents.application.port.out.UserContextPort;
import com.aiworkspace.documents.presentation.request.ChatWithDocumentRequest;
import com.aiworkspace.documents.presentation.response.ChatResponse;
import com.aiworkspace.shared.presentation.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents/{documentId}/chat")
public class DocumentChatController {

    private final ChatWithDocumentUseCase chatUseCase;
    private final UserContextPort userContextPort;

    public DocumentChatController(ChatWithDocumentUseCase chatUseCase,
                                   UserContextPort userContextPort) {
        this.chatUseCase = chatUseCase;
        this.userContextPort = userContextPort;
    }

    @PostMapping
    public ApiResponse<ChatResponse> chat(
            @PathVariable UUID documentId,
            @Valid @RequestBody ChatWithDocumentRequest request) {

        UUID ownerId = UUID.fromString(userContextPort.getCurrentUserId());

        ChatWithDocumentDto result = chatUseCase.chat(
                new ChatWithDocumentCommand(documentId, ownerId, request.question()));

        return ApiResponse.ok(ChatResponse.from(result));
    }
}
