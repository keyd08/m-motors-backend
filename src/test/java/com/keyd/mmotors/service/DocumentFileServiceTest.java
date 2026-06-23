package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.DocumentFileRequest;
import com.keyd.mmotors.entity.*;
import com.keyd.mmotors.exception.BusinessRuleException;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.DocumentFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentFileServiceTest {

    @Mock
    private DocumentFileRepository documentFileRepository;

    @Mock
    private ApplicationFileRepository applicationFileRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private DocumentFileService documentFileService;

    @TempDir
    private Path temporaryDirectory;

    @Test
    @DisplayName("Doit ajouter un document justificatif à un dossier client")
    void shouldCreateDocumentFileForClientApplicationFile() {
        AppUser client = buildClient();
        ApplicationFile applicationFile = buildApplicationFile(client);

        DocumentFileRequest request = new DocumentFileRequest(
                DocumentType.IDENTITY_DOCUMENT,
                "piece-identite.pdf",
                "/uploads/piece-identite.pdf",
                "application/pdf",
                250000L
        );

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));
        when(documentFileRepository.save(org.mockito.ArgumentMatchers.any(DocumentFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentFile result = documentFileService.createDocumentFile(
                "client@mmotors.demo",
                100L,
                request
        );

        assertThat(result.getType()).isEqualTo(DocumentType.IDENTITY_DOCUMENT);
        assertThat(result.getFileName()).isEqualTo("piece-identite.pdf");
        assertThat(result.getApplicationFile().getId()).isEqualTo(100L);

        ArgumentCaptor<DocumentFile> captor = ArgumentCaptor.forClass(DocumentFile.class);
        verify(documentFileRepository).save(captor.capture());

        assertThat(captor.getValue().getContentType()).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("Doit refuser un document trop volumineux")
    void shouldRejectTooLargeDocumentFile() {
        AppUser client = buildClient();
        ApplicationFile applicationFile = buildApplicationFile(client);

        DocumentFileRequest request = new DocumentFileRequest(
                DocumentType.PROOF_OF_ADDRESS,
                "justificatif.pdf",
                "/uploads/justificatif.pdf",
                "application/pdf",
                6000000L
        );

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));

        assertThatThrownBy(() -> documentFileService.createDocumentFile(
                "client@mmotors.demo",
                100L,
                request
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("5 Mo");
    }

    @Test
    @DisplayName("Doit retourner les documents associés à un dossier client")
    void shouldReturnClientDocuments() {
        AppUser client = buildClient();
        ApplicationFile applicationFile = buildApplicationFile(client);

        DocumentFile documentFile = DocumentFile.builder()
                .id(200L)
                .type(DocumentType.BANK_DETAILS)
                .fileName("rib.pdf")
                .filePath("/uploads/rib.pdf")
                .contentType("application/pdf")
                .size(120000L)
                .applicationFile(applicationFile)
                .build();

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));
        when(documentFileRepository.findByApplicationFileId(100L)).thenReturn(List.of(documentFile));

        List<DocumentFile> result = documentFileService.findClientDocuments(
                "client@mmotors.demo",
                100L
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("rib.pdf");

        verify(documentFileRepository).findByApplicationFileId(100L);
    }

    @Test
    @DisplayName("Doit téléverser un document justificatif")
    void shouldUploadDocumentFileForClientApplicationFile() throws Exception {
        AppUser client = buildClient();
        ApplicationFile applicationFile = buildApplicationFile(client);
        MultipartFile multipartFile = buildMultipartFile(
                "piece identité.pdf",
                "application/pdf",
                "contenu pdf"
        );

        ReflectionTestUtils.setField(
                documentFileService,
                "documentUploadDirectory",
                temporaryDirectory.toString()
        );

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));
        when(documentFileRepository.save(org.mockito.ArgumentMatchers.any(DocumentFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentFile result = documentFileService.uploadDocumentFile(
                "client@mmotors.demo",
                100L,
                DocumentType.IDENTITY_DOCUMENT,
                multipartFile
        );

        assertThat(result.getFileName()).isEqualTo("piece identité.pdf");
        assertThat(result.getContentType()).isEqualTo("application/pdf");
        assertThat(result.getSize()).isEqualTo(multipartFile.getSize());
        assertThat(Path.of(result.getFilePath())).exists();

        ArgumentCaptor<DocumentFile> captor = ArgumentCaptor.forClass(DocumentFile.class);
        verify(documentFileRepository).save(captor.capture());

        assertThat(captor.getValue().getFilePath()).contains("piece_identit_.pdf");
    }

    @Test
    @DisplayName("Doit refuser le téléversement d'un fichier vide")
    void shouldRejectEmptyUploadedDocumentFile() {
        AppUser client = buildClient();
        ApplicationFile applicationFile = buildApplicationFile(client);
        MultipartFile multipartFile = buildMultipartFile(
                "vide.pdf",
                "application/pdf",
                ""
        );

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));

        assertThatThrownBy(() -> documentFileService.uploadDocumentFile(
                "client@mmotors.demo",
                100L,
                DocumentType.IDENTITY_DOCUMENT,
                multipartFile
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("obligatoire");
    }

    @Test
    @DisplayName("Doit refuser le téléversement d'un format non autorisé")
    void shouldRejectUnsupportedUploadedDocumentFile() {
        AppUser client = buildClient();
        ApplicationFile applicationFile = buildApplicationFile(client);
        MultipartFile multipartFile = buildMultipartFile(
                "script.exe",
                "application/octet-stream",
                "contenu"
        );

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));

        assertThatThrownBy(() -> documentFileService.uploadDocumentFile(
                "client@mmotors.demo",
                100L,
                DocumentType.OTHER,
                multipartFile
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("format");
    }

    @Test
    @DisplayName("Doit autoriser l'administrateur à télécharger un document")
    void shouldFindDownloadableDocumentForAdmin() {
        AppUser admin = AppUser.builder()
                .id(9L)
                .firstName("Admin")
                .lastName("Demo")
                .email("admin@mmotors.demo")
                .password("encoded-password")
                .role(Role.ADMIN)
                .build();

        DocumentFile documentFile = buildStoredDocumentFile(buildApplicationFile(buildClient()));

        when(appUserRepository.findByEmail("admin@mmotors.demo")).thenReturn(Optional.of(admin));
        when(documentFileRepository.findById(200L)).thenReturn(Optional.of(documentFile));

        DocumentFile result = documentFileService.findDownloadableDocument("admin@mmotors.demo", 200L);

        assertThat(result.getId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("Doit autoriser le client propriétaire à télécharger son document")
    void shouldFindDownloadableDocumentForOwnerClient() {
        AppUser client = buildClient();
        DocumentFile documentFile = buildStoredDocumentFile(buildApplicationFile(client));

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(documentFileRepository.findById(200L)).thenReturn(Optional.of(documentFile));

        DocumentFile result = documentFileService.findDownloadableDocument("client@mmotors.demo", 200L);

        assertThat(result.getFileName()).isEqualTo("piece-identite.pdf");
    }

    @Test
    @DisplayName("Doit refuser le téléchargement d'un document d'un autre client")
    void shouldRejectDownloadForAnotherClient() {
        AppUser owner = buildClient();
        AppUser otherClient = AppUser.builder()
                .id(2L)
                .firstName("Autre")
                .lastName("Client")
                .email("autre@mmotors.demo")
                .password("encoded-password")
                .role(Role.CLIENT)
                .build();

        DocumentFile documentFile = buildStoredDocumentFile(buildApplicationFile(owner));

        when(appUserRepository.findByEmail("autre@mmotors.demo")).thenReturn(Optional.of(otherClient));
        when(documentFileRepository.findById(200L)).thenReturn(Optional.of(documentFile));

        assertThatThrownBy(() -> documentFileService.findDownloadableDocument("autre@mmotors.demo", 200L))
                .isInstanceOf(com.keyd.mmotors.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Document introuvable");
    }

    @Test
    @DisplayName("Doit charger une ressource de document téléchargeable")
    void shouldLoadDocumentResource() throws Exception {
        Path filePath = temporaryDirectory.resolve("piece-identite.pdf");
        Files.writeString(filePath, "contenu pdf");

        DocumentFile documentFile = buildStoredDocumentFile(buildApplicationFile(buildClient()));
        documentFile.setFilePath(filePath.toString());

        assertThat(documentFileService.loadDocumentResource(documentFile).exists()).isTrue();
    }

    private MultipartFile buildMultipartFile(String fileName, String contentType, String content) {
        return new org.springframework.mock.web.MockMultipartFile(
                "file",
                fileName,
                contentType,
                content.getBytes()
        );
    }

    private DocumentFile buildStoredDocumentFile(ApplicationFile applicationFile) {
        return DocumentFile.builder()
                .id(200L)
                .type(DocumentType.IDENTITY_DOCUMENT)
                .fileName("piece-identite.pdf")
                .filePath("/uploads/piece-identite.pdf")
                .contentType("application/pdf")
                .size(250000L)
                .applicationFile(applicationFile)
                .build();
    }

    private AppUser buildClient() {
        return AppUser.builder()
                .id(1L)
                .firstName("Client")
                .lastName("Demo")
                .email("client@mmotors.demo")
                .password("encoded-password")
                .role(Role.CLIENT)
                .build();
    }

    private ApplicationFile buildApplicationFile(AppUser client) {
        Vehicle vehicle = Vehicle.builder()
                .id(10L)
                .brand("Peugeot")
                .model("308")
                .energy("Diesel")
                .mileage(85000)
                .mode(VehicleMode.SALE)
                .available(true)
                .build();

        return ApplicationFile.builder()
                .id(100L)
                .type(ApplicationType.PURCHASE)
                .status(ApplicationStatus.INCOMPLETE)
                .client(client)
                .vehicle(vehicle)
                .build();
    }
    @Test
    @DisplayName("Doit supprimer un document client avant l'envoi du dossier")
    void shouldDeleteClientDocumentBeforeSubmission() throws Exception {
        AppUser client = buildClient();
        ApplicationFile applicationFile = buildApplicationFile(client);
        applicationFile.setStatus(ApplicationStatus.INCOMPLETE);

        Path storedFile = temporaryDirectory.resolve("piece-identite.pdf");
        Files.writeString(storedFile, "contenu pdf");

        DocumentFile documentFile = buildStoredDocumentFile(applicationFile);
        documentFile.setFilePath(storedFile.toString());

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(documentFileRepository.findById(200L)).thenReturn(Optional.of(documentFile));

        documentFileService.deleteClientDocument("client@mmotors.demo", 200L);

        assertThat(storedFile).doesNotExist();
        verify(documentFileRepository).delete(documentFile);
    }

    @Test
    @DisplayName("Doit refuser la suppression d'un document après l'envoi du dossier")
    void shouldRejectDocumentDeletionAfterSubmission() {
        AppUser client = buildClient();
        ApplicationFile applicationFile = buildApplicationFile(client);
        applicationFile.setStatus(ApplicationStatus.SUBMITTED);

        DocumentFile documentFile = buildStoredDocumentFile(applicationFile);

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(documentFileRepository.findById(200L)).thenReturn(Optional.of(documentFile));

        assertThatThrownBy(() -> documentFileService.deleteClientDocument("client@mmotors.demo", 200L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ne peut plus être modifié");
    }

}
