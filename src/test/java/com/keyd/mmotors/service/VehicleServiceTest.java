package com.keyd.mmotors.service;

import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import com.keyd.mmotors.exception.ResourceNotFoundException;
import com.keyd.mmotors.repository.VehicleRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    @DisplayName("Doit rechercher les véhicules disponibles selon le mode demandé")
    void shouldFindAvailableVehiclesByMode() {
        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .brand("Renault")
                .model("Clio")
                .energy("Essence")
                .mileage(42000)
                .monthlyPrice(new BigDecimal("249"))
                .mode(VehicleMode.RENTAL)
                .available(true)
                .build();

        when(vehicleRepository.findByModeAndAvailableTrue(VehicleMode.RENTAL))
                .thenReturn(List.of(vehicle));

        List<Vehicle> result = vehicleService.findAvailableVehicles(VehicleMode.RENTAL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("Renault");
        assertThat(result.get(0).getMode()).isEqualTo(VehicleMode.RENTAL);

        verify(vehicleRepository).findByModeAndAvailableTrue(VehicleMode.RENTAL);
    }

    @Test
    @DisplayName("Doit retourner une erreur si le véhicule demandé n'existe pas")
    void shouldThrowExceptionWhenVehicleDoesNotExist() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Véhicule introuvable");
    }

    @Test
    @DisplayName("Doit basculer un véhicule vers un autre mode")
    void shouldSwitchVehicleMode() {
        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .brand("Peugeot")
                .model("308")
                .energy("Diesel")
                .mileage(85000)
                .price(new BigDecimal("12900"))
                .mode(VehicleMode.SALE)
                .available(true)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        Vehicle result = vehicleService.switchVehicleMode(1L, VehicleMode.RENTAL);

        assertThat(result.getMode()).isEqualTo(VehicleMode.RENTAL);

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(vehicle);
    }
}
