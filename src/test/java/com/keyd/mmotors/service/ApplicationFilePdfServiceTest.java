package com.keyd.mmotors.service;

import com.keyd.mmotors.entity.*;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.DocumentFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationFilePdfServiceTest {

    @Mock
    private ApplicationFileService applicationFileService;

    @Mock
    private ApplicationFileRepository applicationFileRepository;

    @Mock
    private DocumentFileRepository documentFileRepository;

    @InjectMocks
    private ApplicationFilePdfService applicationFilePdfService;

    @Test
    @DisplayName("Doit générer le PDF récapitulatif d'un dossier client")
    void shouldGenerateClientSummaryPdf() {
        ApplicationFile applicationFile = buildApplicationFile();
        DocumentFile documentFile = buildDocumentFile(applicationFile);

        when(applicationFileService.findClientApplicationFileById("client@mmotors.demo", 100L))
                .thenReturn(applicationFile);
        when(documentFileRepository.findByApplicationFileId(100L)).thenReturn(List.of(documentFile));

        byte[] result = applicationFilePdfService.generateClientSummaryPdf("client@mmotors.demo", 100L);

        assertThat(result).isNotEmpty();
        assertThat(new String(result, 0, 4)).isEqualTo("%PDF");

        verify(applicationFileService).findClientApplicationFileById("client@mmotors.demo", 100L);
        verify(documentFileRepository).findByApplicationFileId(100L);
    }

    @Test
    @DisplayName("Doit générer le PDF récapitulatif d'un dossier côté administration")
    void shouldGenerateAdminSummaryPdf() {
        ApplicationFile applicationFile = buildApplicationFile();
        DocumentFile documentFile = buildDocumentFile(applicationFile);

        when(applicationFileRepository.findById(100L)).thenReturn(Optional.of(applicationFile));
        when(documentFileRepository.findByApplicationFileId(100L)).thenReturn(List.of(documentFile));

        byte[] result = applicationFilePdfService.generateAdminSummaryPdf(100L);

        assertThat(result).isNotEmpty();
        assertThat(new String(result, 0, 4)).isEqualTo("%PDF");

        verify(applicationFileRepository).findById(100L);
        verify(documentFileRepository).findByApplicationFileId(100L);
    }

    private ApplicationFile buildApplicationFile() {
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
                .price(new BigDecimal("12900"))
                .mode(VehicleMode.SALE)
                .available(true)
                .description("Véhicule révisé")
                .build();

        return ApplicationFile.builder()
                .id(100L)
                .type(ApplicationType.PURCHASE)
                .status(ApplicationStatus.SUBMITTED)
                .client(client)
                .vehicle(vehicle)
                .build();
    }

    private DocumentFile buildDocumentFile(ApplicationFile applicationFile) {
        return DocumentFile.builder()
                .id(200L)
                .type(DocumentType.IDENTITY_DOCUMENT)
                .fileName("piece-identite.pdf")
                .filePath("uploads/document-files/piece-identite.pdf")
                .contentType("application/pdf")
                .size(120000L)
                .applicationFile(applicationFile)
                .build();
    }
}
