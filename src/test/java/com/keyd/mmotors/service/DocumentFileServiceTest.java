package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.DocumentFileRequest;
import com.keyd.mmotors.entity.*;
import com.keyd.mmotors.exception.BusinessRuleException;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.DocumentFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .status(ApplicationStatus.SUBMITTED)
                .client(client)
                .vehicle(vehicle)
                .build();
    }
}
