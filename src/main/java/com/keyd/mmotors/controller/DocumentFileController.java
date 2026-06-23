package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.DocumentFileRequest;
import com.keyd.mmotors.dto.DocumentFileResponse;
import com.keyd.mmotors.entity.DocumentFile;
import com.keyd.mmotors.entity.DocumentType;
import com.keyd.mmotors.service.DocumentFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(
            value = "/application-files/{applicationFileId}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentFileResponse uploadDocumentFile(
            Principal principal,
            @PathVariable Long applicationFileId,
            @RequestParam DocumentType type,
            @RequestParam MultipartFile file
    ) {
        DocumentFile uploadedDocumentFile = documentFileService.uploadDocumentFile(
                principal.getName(),
                applicationFileId,
                type,
                file
        );

        return DocumentFileResponse.fromEntity(uploadedDocumentFile);
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


    @DeleteMapping("/{documentFileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyDocumentFile(
            Principal principal,
            @PathVariable Long documentFileId
    ) {
        documentFileService.deleteClientDocument(principal.getName(), documentFileId);
    }

    @GetMapping("/{documentFileId}/download")
    public ResponseEntity<Resource> downloadDocumentFile(
            Principal principal,
            @PathVariable Long documentFileId
    ) {
        DocumentFile documentFile = documentFileService.findDownloadableDocument(
                principal.getName(),
                documentFileId
        );

        Resource resource = documentFileService.loadDocumentResource(documentFile);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documentFile.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(documentFile.getFileName())
                                .build()
                                .toString()
                )
                .body(resource);
    }
}
