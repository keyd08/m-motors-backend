package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.DocumentFileRequest;
import com.keyd.mmotors.entity.AppUser;
import com.keyd.mmotors.entity.ApplicationFile;
import com.keyd.mmotors.entity.DocumentFile;
import com.keyd.mmotors.entity.Role;
import com.keyd.mmotors.entity.DocumentType;
import com.keyd.mmotors.exception.BusinessRuleException;
import com.keyd.mmotors.exception.ResourceNotFoundException;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.DocumentFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

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

    @Value("${app.upload.document-directory:uploads/document-files}")
    private String documentUploadDirectory;

    public DocumentFile createDocumentFile(
            String clientEmail,
            Long applicationFileId,
            DocumentFileRequest request
    ) {
        ApplicationFile applicationFile = findClientApplicationFile(clientEmail, applicationFileId);

        validateDocumentFile(request.contentType(), request.size());

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

    public DocumentFile uploadDocumentFile(
            String clientEmail,
            Long applicationFileId,
            DocumentType type,
            MultipartFile file
    ) {
        ApplicationFile applicationFile = findClientApplicationFile(clientEmail, applicationFileId);

        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Le fichier est obligatoire");
        }

        validateDocumentFile(file.getContentType(), file.getSize());

        String originalFileName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename()
        );

        if (originalFileName.contains("..")) {
            throw new BusinessRuleException("Le nom du fichier n'est pas autorisé");
        }

        String safeFileName = originalFileName.replaceAll("[^A-Za-z0-9._-]", "_");
        String storedFileName = UUID.randomUUID() + "-" + safeFileName;

        try {
            Path uploadDirectory = Paths.get(documentUploadDirectory).toAbsolutePath().normalize();
            Files.createDirectories(uploadDirectory);

            Path destination = uploadDirectory.resolve(storedFileName).normalize();

            if (!destination.startsWith(uploadDirectory)) {
                throw new BusinessRuleException("Le chemin du fichier n'est pas autorisé");
            }

            file.transferTo(destination);

            DocumentFile documentFile = DocumentFile.builder()
                    .type(type)
                    .fileName(originalFileName)
                    .filePath(destination.toString())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .applicationFile(applicationFile)
                    .build();

            return documentFileRepository.save(documentFile);
        } catch (IOException exception) {
            throw new BusinessRuleException("Impossible d'enregistrer le fichier");
        }
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

    public DocumentFile findDownloadableDocument(String userEmail, Long documentFileId) {
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userEmail));

        DocumentFile documentFile = documentFileRepository.findById(documentFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable avec l'identifiant : " + documentFileId));

        if (user.getRole() == Role.ADMIN) {
            return documentFile;
        }

        Long documentClientId = documentFile.getApplicationFile().getClient().getId();

        if (!documentClientId.equals(user.getId())) {
            throw new ResourceNotFoundException("Document introuvable pour cet utilisateur");
        }

        return documentFile;
    }

    public Resource loadDocumentResource(DocumentFile documentFile) {
        try {
            Path filePath = Paths.get(documentFile.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Fichier introuvable");
            }

            return resource;
        } catch (MalformedURLException exception) {
            throw new ResourceNotFoundException("Fichier introuvable");
        }
    }

    private ApplicationFile findClientApplicationFile(String clientEmail, Long applicationFileId) {
        AppUser client = appUserRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientEmail));

        return applicationFileRepository.findByIdAndClientId(applicationFileId, client.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable pour ce client"));
    }

    private void validateDocumentFile(String contentType, Long size) {
        if (size == null || size <= 0) {
            throw new BusinessRuleException("La taille du fichier doit être positive");
        }

        if (size > MAX_FILE_SIZE) {
            throw new BusinessRuleException("La taille du fichier ne doit pas dépasser 5 Mo");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessRuleException("Le format du fichier n'est pas autorisé");
        }
    }
}
