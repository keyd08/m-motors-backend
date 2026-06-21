package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.DocumentFileRequest;
import com.keyd.mmotors.dto.DocumentFileResponse;
import com.keyd.mmotors.entity.*;
import com.keyd.mmotors.service.DocumentFileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentFileControllerTest {

    @Mock
    private DocumentFileService documentFileService;

    @InjectMocks
    private DocumentFileController documentFileController;

    @Test
    @DisplayName("Doit créer un document justificatif depuis une requête API")
    void shouldCreateDocumentFile() {
        Principal principal = () -> "client@mmotors.demo";

        DocumentFileRequest request = new DocumentFileRequest(
                DocumentType.IDENTITY_DOCUMENT,
                "piece-identite.pdf",
                "/uploads/piece-identite.pdf",
                "application/pdf",
                250000L
        );

        DocumentFile documentFile = buildDocumentFile();

        when(documentFileService.createDocumentFile("client@mmotors.demo", 100L, request))
                .thenReturn(documentFile);

        DocumentFileResponse result = documentFileController
                .createDocumentFile(principal, 100L, request);

        assertThat(result.id()).isEqualTo(200L);
        assertThat(result.type()).isEqualTo(DocumentType.IDENTITY_DOCUMENT);
        assertThat(result.fileName()).isEqualTo("piece-identite.pdf");
        assertThat(result.applicationFileId()).isEqualTo(100L);

        verify(documentFileService).createDocumentFile("client@mmotors.demo", 100L, request);
    }

    @Test
    @DisplayName("Doit téléverser un document depuis une requête multipart")
    void shouldUploadDocumentFile() {
        Principal principal = () -> "client@mmotors.demo";
        MultipartFile multipartFile = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "piece-identite.pdf",
                "application/pdf",
                "contenu pdf".getBytes()
        );

        DocumentFile documentFile = buildDocumentFile();

        when(documentFileService.uploadDocumentFile(
                "client@mmotors.demo",
                100L,
                DocumentType.IDENTITY_DOCUMENT,
                multipartFile
        )).thenReturn(documentFile);

        DocumentFileResponse result = documentFileController.uploadDocumentFile(
                principal,
                100L,
                DocumentType.IDENTITY_DOCUMENT,
                multipartFile
        );

        assertThat(result.fileName()).isEqualTo("piece-identite.pdf");

        verify(documentFileService).uploadDocumentFile(
                "client@mmotors.demo",
                100L,
                DocumentType.IDENTITY_DOCUMENT,
                multipartFile
        );
    }

    @Test
    @DisplayName("Doit retourner un document en téléchargement")
    void shouldDownloadDocumentFile() {
        Principal principal = () -> "client@mmotors.demo";
        DocumentFile documentFile = buildDocumentFile();
        ByteArrayResource resource = new ByteArrayResource("contenu pdf".getBytes());

        when(documentFileService.findDownloadableDocument("client@mmotors.demo", 200L))
                .thenReturn(documentFile);
        when(documentFileService.loadDocumentResource(documentFile)).thenReturn(resource);

        ResponseEntity<org.springframework.core.io.Resource> result =
                documentFileController.downloadDocumentFile(principal, 200L);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getHeaders().getContentDisposition().getFilename())
                .isEqualTo("piece-identite.pdf");

        verify(documentFileService).findDownloadableDocument("client@mmotors.demo", 200L);
        verify(documentFileService).loadDocumentResource(documentFile);
    }

    @Test
    @DisplayName("Doit retourner les documents du dossier du client connecté")
    void shouldReturnCurrentClientDocuments() {
        Principal principal = () -> "client@mmotors.demo";
        DocumentFile documentFile = buildDocumentFile();

        when(documentFileService.findClientDocuments("client@mmotors.demo", 100L))
                .thenReturn(List.of(documentFile));

        List<DocumentFileResponse> result = documentFileController
                .findMyDocuments(principal, 100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).fileName()).isEqualTo("piece-identite.pdf");

        verify(documentFileService).findClientDocuments("client@mmotors.demo", 100L);
    }

    @Test
    @DisplayName("Doit retourner les documents d'un dossier côté administration")
    void shouldReturnAdminDocuments() {
        DocumentFile documentFile = buildDocumentFile();

        when(documentFileService.findAdminDocuments(100L))
                .thenReturn(List.of(documentFile));

        List<DocumentFileResponse> result = documentFileController
                .findAdminDocuments(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).applicationFileId()).isEqualTo(100L);

        verify(documentFileService).findAdminDocuments(100L);
    }

    private DocumentFile buildDocumentFile() {
        AppUser client = AppUser.builder()
                .id(1L)
                .firstName("Client")
                .lastName("Demo")
                .email("client@mmotors.demo")
                .password("encoded-password")
                .role(Role.CLIENT)
                .build();

        Vehicle vehicle = Vehicle.builder()
                .id(10L)
                .brand("Peugeot")
                .model("308")
                .energy("Diesel")
                .mileage(85000)
                .mode(VehicleMode.SALE)
                .available(true)
                .build();

        ApplicationFile applicationFile = ApplicationFile.builder()
                .id(100L)
                .type(ApplicationType.PURCHASE)
                .status(ApplicationStatus.SUBMITTED)
                .client(client)
                .vehicle(vehicle)
                .build();

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
}
