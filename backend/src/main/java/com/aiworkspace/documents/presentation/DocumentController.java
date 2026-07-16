package com.aiworkspace.documents.presentation;

import com.aiworkspace.documents.application.command.DeleteDocumentCommand;
import com.aiworkspace.documents.application.command.UploadDocumentCommand;
import com.aiworkspace.documents.application.dto.DocumentDto;
import com.aiworkspace.documents.application.port.in.DeleteDocumentUseCase;
import com.aiworkspace.documents.application.port.in.GetDocumentUseCase;
import com.aiworkspace.documents.application.port.in.ListDocumentsUseCase;
import com.aiworkspace.documents.application.port.in.UploadDocumentUseCase;
import com.aiworkspace.documents.application.port.out.UserContextPort;
import com.aiworkspace.documents.presentation.response.DocumentResponse;
import com.aiworkspace.shared.exception.DomainException;
import com.aiworkspace.shared.presentation.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final UploadDocumentUseCase uploadUseCase;
    private final ListDocumentsUseCase listUseCase;
    private final GetDocumentUseCase getUseCase;
    private final DeleteDocumentUseCase deleteUseCase;
    private final UserContextPort userContextPort;

    public DocumentController(UploadDocumentUseCase uploadUseCase,
                               ListDocumentsUseCase listUseCase,
                               GetDocumentUseCase getUseCase,
                               DeleteDocumentUseCase deleteUseCase,
                               UserContextPort userContextPort) {
        this.uploadUseCase = uploadUseCase;
        this.listUseCase = listUseCase;
        this.getUseCase = getUseCase;
        this.deleteUseCase = deleteUseCase;
        this.userContextPort = userContextPort;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<DocumentResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {

        UUID ownerId = UUID.fromString(userContextPort.getCurrentUserId());
        String resolvedTitle = (title != null && !title.isBlank())
                ? title : stripExtension(file.getOriginalFilename());

        try {
            UploadDocumentCommand command = new UploadDocumentCommand(
                    ownerId, resolvedTitle, file.getOriginalFilename(),
                    file.getContentType(), file.getBytes());

            DocumentDto dto = uploadUseCase.upload(command);
            return ApiResponse.ok("Document uploaded and queued for indexing",
                    DocumentResponse.from(dto));
        } catch (java.io.IOException e) {
            throw new DomainException("Failed to read uploaded file: " + e.getMessage(), e);
        }
    }

    @GetMapping
    public ApiResponse<List<DocumentResponse>> list() {
        UUID ownerId = UUID.fromString(userContextPort.getCurrentUserId());
        List<DocumentResponse> docs = listUseCase.listByOwner(ownerId)
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return ApiResponse.ok(docs);
    }

    @GetMapping("/{id}")
    public ApiResponse<DocumentResponse> get(@PathVariable UUID id) {
        UUID ownerId = UUID.fromString(userContextPort.getCurrentUserId());
        DocumentDto dto = getUseCase.getById(id, ownerId);
        return ApiResponse.ok(DocumentResponse.from(dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        UUID ownerId = UUID.fromString(userContextPort.getCurrentUserId());
        deleteUseCase.delete(new DeleteDocumentCommand(id, ownerId));
    }

    private String stripExtension(String fileName) {
        if (fileName == null) return "document";
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}
