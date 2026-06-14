package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.VehicleRequest;
import com.keyd.mmotors.dto.VehicleResponse;
import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import com.keyd.mmotors.service.VehicleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private VehicleController vehicleController;

    @Test
    @DisplayName("Doit retourner les véhicules disponibles sous forme de réponse API")
    void shouldReturnAvailableVehicles() {
        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .brand("Renault")
                .model("Clio")
                .energy("Essence")
                .mileage(42000)
                .monthlyPrice(new BigDecimal("249"))
                .mode(VehicleMode.RENTAL)
                .available(true)
                .description("Véhicule disponible à la location")
                .build();

        when(vehicleService.findAvailableVehicles(VehicleMode.RENTAL))
                .thenReturn(List.of(vehicle));

        List<VehicleResponse> result = vehicleController.findAvailableVehicles(VehicleMode.RENTAL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).brand()).isEqualTo("Renault");
        assertThat(result.get(0).mode()).isEqualTo(VehicleMode.RENTAL);

        verify(vehicleService).findAvailableVehicles(VehicleMode.RENTAL);
    }

    @Test
    @DisplayName("Doit créer un véhicule depuis une requête API")
    void shouldCreateVehicle() {
        VehicleRequest request = new VehicleRequest(
                "Peugeot",
                "308",
                "Diesel",
                85000,
                new BigDecimal("12900"),
                null,
                VehicleMode.SALE,
                true,
                "Véhicule disponible à l'achat"
        );

        Vehicle savedVehicle = request.toEntity();
        savedVehicle.setId(1L);

        when(vehicleService.createVehicle(any(Vehicle.class)))
                .thenReturn(savedVehicle);

        VehicleResponse result = vehicleController.createVehicle(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.brand()).isEqualTo("Peugeot");
        assertThat(result.mode()).isEqualTo(VehicleMode.SALE);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleService).createVehicle(vehicleCaptor.capture());

        Vehicle capturedVehicle = vehicleCaptor.getValue();

        assertThat(capturedVehicle.getBrand()).isEqualTo("Peugeot");
        assertThat(capturedVehicle.getModel()).isEqualTo("308");
        assertThat(capturedVehicle.getMode()).isEqualTo(VehicleMode.SALE);
    }
}
