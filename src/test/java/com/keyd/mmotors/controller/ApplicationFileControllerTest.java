package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.ApplicationFileRequest;
import com.keyd.mmotors.dto.ApplicationFileResponse;
import com.keyd.mmotors.dto.ApplicationFileStatusRequest;
import com.keyd.mmotors.entity.*;
import com.keyd.mmotors.service.ApplicationFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationFileControllerTest {

    @Mock
    private ApplicationFileService applicationFileService;

    @InjectMocks
    private ApplicationFileController applicationFileController;

    @Test
    @DisplayName("Doit créer un dossier client depuis une requête API")
    void shouldCreateApplicationFile() {
        Principal principal = () -> "client@mmotors.demo";

        ApplicationFileRequest request = new ApplicationFileRequest(
                ApplicationType.PURCHASE,
                10L
        );

        ApplicationFile applicationFile = buildApplicationFile();

        when(applicationFileService.createApplicationFile("client@mmotors.demo", request))
                .thenReturn(applicationFile);

        ApplicationFileResponse result = applicationFileController
                .createApplicationFile(principal, request);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.type()).isEqualTo(ApplicationType.PURCHASE);
        assertThat(result.status()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(result.clientEmail()).isEqualTo("client@mmotors.demo");
        assertThat(result.vehicleId()).isEqualTo(10L);

        verify(applicationFileService).createApplicationFile("client@mmotors.demo", request);
    }

    @Test
    @DisplayName("Doit retourner les dossiers du client connecté")
    void shouldReturnCurrentClientApplicationFiles() {
        Principal principal = () -> "client@mmotors.demo";
        ApplicationFile applicationFile = buildApplicationFile();

        when(applicationFileService.findClientApplicationFiles("client@mmotors.demo"))
                .thenReturn(List.of(applicationFile));

        List<ApplicationFileResponse> result = applicationFileController
                .findMyApplicationFiles(principal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clientEmail()).isEqualTo("client@mmotors.demo");

        verify(applicationFileService).findClientApplicationFiles("client@mmotors.demo");
    }

    @Test
    @DisplayName("Doit mettre à jour le statut d'un dossier côté administration")
    void shouldUpdateApplicationFileStatus() {
        ApplicationFileStatusRequest request = new ApplicationFileStatusRequest(
                ApplicationStatus.APPROVED,
                "Dossier validé"
        );

        ApplicationFile applicationFile = buildApplicationFile();
        applicationFile.setStatus(ApplicationStatus.APPROVED);
        applicationFile.setAdminComment("Dossier validé");

        when(applicationFileService.updateApplicationFileStatus(100L, request))
                .thenReturn(applicationFile);

        ApplicationFileResponse result = applicationFileController
                .updateApplicationFileStatus(100L, request);

        assertThat(result.status()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(result.adminComment()).isEqualTo("Dossier validé");

        verify(applicationFileService).updateApplicationFileStatus(100L, request);
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
