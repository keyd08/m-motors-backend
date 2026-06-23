package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.ApplicationFileRequest;
import com.keyd.mmotors.dto.ApplicationFileStatusRequest;
import com.keyd.mmotors.entity.*;
import com.keyd.mmotors.exception.BusinessRuleException;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.DocumentFileRepository;
import com.keyd.mmotors.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationFileServiceTest {

    @Mock
    private ApplicationFileRepository applicationFileRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DocumentFileRepository documentFileRepository;

    @InjectMocks
    private ApplicationFileService applicationFileService;

    @Test
    @DisplayName("Doit créer un dossier d'achat pour un véhicule en vente")
    void shouldCreatePurchaseApplicationFile() {
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
                .build();

        ApplicationFileRequest request = new ApplicationFileRequest(ApplicationType.PURCHASE, 10L);

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(applicationFileRepository.save(org.mockito.ArgumentMatchers.any(ApplicationFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationFile result = applicationFileService.createApplicationFile("client@mmotors.demo", request);

        assertThat(result.getType()).isEqualTo(ApplicationType.PURCHASE);
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.INCOMPLETE);
        assertThat(result.getClient().getEmail()).isEqualTo("client@mmotors.demo");
        assertThat(result.getVehicle().getId()).isEqualTo(10L);

        ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
        verify(applicationFileRepository).save(captor.capture());

        assertThat(captor.getValue().getType()).isEqualTo(ApplicationType.PURCHASE);
    }

    @Test
    @DisplayName("Doit refuser un dossier d'achat sur un véhicule en location")
    void shouldRejectPurchaseApplicationForRentalVehicle() {
        AppUser client = AppUser.builder()
                .id(1L)
                .email("client@mmotors.demo")
                .password("encoded-password")
                .role(Role.CLIENT)
                .firstName("Client")
                .lastName("Demo")
                .build();

        Vehicle rentalVehicle = Vehicle.builder()
                .id(20L)
                .brand("Renault")
                .model("Clio")
                .energy("Essence")
                .mileage(42000)
                .monthlyPrice(new BigDecimal("249"))
                .mode(VehicleMode.RENTAL)
                .available(true)
                .build();

        ApplicationFileRequest request = new ApplicationFileRequest(ApplicationType.PURCHASE, 20L);

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(vehicleRepository.findById(20L)).thenReturn(Optional.of(rentalVehicle));

        assertThatThrownBy(() -> applicationFileService.createApplicationFile("client@mmotors.demo", request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("dossier d'achat");
    }

    @Test
    @DisplayName("Doit mettre à jour le statut d'un dossier")
    void shouldUpdateApplicationFileStatus() {
        ApplicationFile applicationFile = ApplicationFile.builder()
                .id(100L)
                .type(ApplicationType.RENTAL)
                .status(ApplicationStatus.SUBMITTED)
                .build();

        ApplicationFileStatusRequest request = new ApplicationFileStatusRequest(
                ApplicationStatus.APPROVED,
                "Dossier validé"
        );

        when(applicationFileRepository.findById(100L)).thenReturn(Optional.of(applicationFile));
        when(applicationFileRepository.save(applicationFile)).thenReturn(applicationFile);

        ApplicationFile result = applicationFileService.updateApplicationFileStatus(100L, request);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(result.getAdminComment()).isEqualTo("Dossier validé");

        verify(applicationFileRepository).save(applicationFile);
    }
    @Test
    @DisplayName("Doit envoyer un dossier client contenant au moins un document")
    void shouldSubmitClientApplicationFileWhenDocumentsExist() {
        AppUser client = AppUser.builder()
                .id(1L)
                .email("client@mmotors.demo")
                .password("encoded-password")
                .role(Role.CLIENT)
                .firstName("Client")
                .lastName("Demo")
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
                .build();

        ApplicationFile applicationFile = ApplicationFile.builder()
                .id(100L)
                .type(ApplicationType.PURCHASE)
                .status(ApplicationStatus.INCOMPLETE)
                .client(client)
                .vehicle(vehicle)
                .build();

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));
        when(documentFileRepository.countByApplicationFileId(100L)).thenReturn(1L);
        when(applicationFileRepository.save(applicationFile)).thenReturn(applicationFile);

        ApplicationFile result = applicationFileService.submitClientApplicationFile("client@mmotors.demo", 100L);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(result.getAdminComment()).isNull();

        verify(documentFileRepository).countByApplicationFileId(100L);
        verify(applicationFileRepository).save(applicationFile);
    }

    @Test
    @DisplayName("Doit refuser l'envoi d'un dossier client sans document")
    void shouldRejectSubmitClientApplicationFileWithoutDocuments() {
        AppUser client = AppUser.builder()
                .id(1L)
                .email("client@mmotors.demo")
                .password("encoded-password")
                .role(Role.CLIENT)
                .firstName("Client")
                .lastName("Demo")
                .build();

        ApplicationFile applicationFile = ApplicationFile.builder()
                .id(100L)
                .type(ApplicationType.PURCHASE)
                .status(ApplicationStatus.INCOMPLETE)
                .client(client)
                .build();

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));
        when(documentFileRepository.countByApplicationFileId(100L)).thenReturn(0L);

        assertThatThrownBy(() -> applicationFileService.submitClientApplicationFile("client@mmotors.demo", 100L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("au moins un document");

        verify(documentFileRepository).countByApplicationFileId(100L);
    }

    @Test
    @DisplayName("Doit supprimer un dossier client en cours")
    void shouldDeleteIncompleteClientApplicationFile() {
        AppUser client = AppUser.builder()
                .id(1L)
                .email("client@mmotors.demo")
                .password("encoded-password")
                .role(Role.CLIENT)
                .firstName("Client")
                .lastName("Demo")
                .build();

        ApplicationFile applicationFile = ApplicationFile.builder()
                .id(100L)
                .type(ApplicationType.PURCHASE)
                .status(ApplicationStatus.INCOMPLETE)
                .client(client)
                .build();

        DocumentFile documentFile = DocumentFile.builder()
                .id(200L)
                .type(DocumentType.IDENTITY_DOCUMENT)
                .fileName("piece-identite.pdf")
                .filePath("target/document-inexistant.pdf")
                .contentType("application/pdf")
                .size(120000L)
                .applicationFile(applicationFile)
                .build();

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));
        when(documentFileRepository.findByApplicationFileId(100L)).thenReturn(java.util.List.of(documentFile));

        applicationFileService.deleteClientApplicationFile("client@mmotors.demo", 100L);

        verify(documentFileRepository).deleteByApplicationFileId(100L);
        verify(applicationFileRepository).delete(applicationFile);
    }

    @Test
    @DisplayName("Doit refuser la suppression d'un dossier déjà envoyé")
    void shouldRejectDeletingSubmittedClientApplicationFile() {
        AppUser client = AppUser.builder()
                .id(1L)
                .email("client@mmotors.demo")
                .password("encoded-password")
                .role(Role.CLIENT)
                .firstName("Client")
                .lastName("Demo")
                .build();

        ApplicationFile applicationFile = ApplicationFile.builder()
                .id(100L)
                .type(ApplicationType.PURCHASE)
                .status(ApplicationStatus.SUBMITTED)
                .client(client)
                .build();

        when(appUserRepository.findByEmail("client@mmotors.demo")).thenReturn(Optional.of(client));
        when(applicationFileRepository.findByIdAndClientId(100L, 1L)).thenReturn(Optional.of(applicationFile));

        assertThatThrownBy(() -> applicationFileService.deleteClientApplicationFile("client@mmotors.demo", 100L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("dossier en cours");
    }

}
