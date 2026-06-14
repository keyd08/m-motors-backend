package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.DocumentFileRequest;
import com.keyd.mmotors.entity.AppUser;
import com.keyd.mmotors.entity.ApplicationFile;
import com.keyd.mmotors.entity.DocumentFile;
import com.keyd.mmotors.exception.BusinessRuleException;
import com.keyd.mmotors.exception.ResourceNotFoundException;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.DocumentFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentFileService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private final DocumentFileRepository documentFileRepository;
    private final ApplicationFileRepository applicationFileRepository;
    private final AppUserRepository appUserRepository;

    public DocumentFile createDocumentFile(
            String clientEmail,
            Long applicationFileId,
            DocumentFileRequest request
    ) {
        ApplicationFile applicationFile = findClientApplicationFile(clientEmail, applicationFileId);

        validateDocumentFile(request);

        DocumentFile documentFile = DocumentFile.builder()
                .type(request.type())
                .fileName(request.fileName())
                .filePath(request.filePath())
                .contentType(request.contentType())
                .size(request.size())
                .applicationFile(applicationFile)
                .build();

        return documentFileRepository.save(documentFile);
    }

    public List<DocumentFile> findClientDocuments(String clientEmail, Long applicationFileId) {
        ApplicationFile applicationFile = findClientApplicationFile(clientEmail, applicationFileId);
        return documentFileRepository.findByApplicationFileId(applicationFile.getId());
    }

    public List<DocumentFile> findAdminDocuments(Long applicationFileId) {
        ApplicationFile applicationFile = applicationFileRepository.findById(applicationFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec l'identifiant : " + applicationFileId));

        return documentFileRepository.findByApplicationFileId(applicationFile.getId());
    }

    private ApplicationFile findClientApplicationFile(String clientEmail, Long applicationFileId) {
        AppUser client = appUserRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientEmail));

        return applicationFileRepository.findByIdAndClientId(applicationFileId, client.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable pour ce client"));
    }

    private void validateDocumentFile(DocumentFileRequest request) {
        if (request.size() > MAX_FILE_SIZE) {
            throw new BusinessRuleException("La taille du fichier ne doit pas dépasser 5 Mo");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
            throw new BusinessRuleException("Le format du fichier n'est pas autorisé");
        }
    }
}
