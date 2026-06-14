package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.DocumentFileRequest;
import com.keyd.mmotors.dto.DocumentFileResponse;
import com.keyd.mmotors.entity.DocumentFile;
import com.keyd.mmotors.service.DocumentFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/document-files")
@RequiredArgsConstructor
public class DocumentFileController {

    private final DocumentFileService documentFileService;

    @PostMapping("/application-files/{applicationFileId}")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentFileResponse createDocumentFile(
            Principal principal,
            @PathVariable Long applicationFileId,
            @Valid @RequestBody DocumentFileRequest request
    ) {
        DocumentFile createdDocumentFile = documentFileService.createDocumentFile(
                principal.getName(),
                applicationFileId,
                request
        );

        return DocumentFileResponse.fromEntity(createdDocumentFile);
    }

    @GetMapping("/application-files/{applicationFileId}")
    public List<DocumentFileResponse> findMyDocuments(
            Principal principal,
            @PathVariable Long applicationFileId
    ) {
        return documentFileService.findClientDocuments(principal.getName(), applicationFileId)
                .stream()
                .map(DocumentFileResponse::fromEntity)
                .toList();
    }

    @GetMapping("/admin/application-files/{applicationFileId}")
    public List<DocumentFileResponse> findAdminDocuments(
            @PathVariable Long applicationFileId
    ) {
        return documentFileService.findAdminDocuments(applicationFileId)
                .stream()
                .map(DocumentFileResponse::fromEntity)
                .toList();
    }
}
