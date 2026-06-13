package com.keyd.mmotors.repository;

import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    @DisplayName("Doit retourner uniquement les véhicules disponibles selon le mode demandé")
    void shouldFindAvailableVehiclesByMode() {
        Vehicle saleVehicle = Vehicle.builder()
                .brand("Peugeot")
                .model("308")
                .energy("Diesel")
                .mileage(85000)
                .price(new BigDecimal("12900"))
                .mode(VehicleMode.SALE)
                .available(true)
                .description("Véhicule disponible à l'achat")
                .build();

        Vehicle rentalVehicle = Vehicle.builder()
                .brand("Renault")
                .model("Clio")
                .energy("Essence")
                .mileage(42000)
                .monthlyPrice(new BigDecimal("249"))
                .mode(VehicleMode.RENTAL)
                .available(true)
                .description("Véhicule disponible à la location")
                .build();

        Vehicle unavailableRentalVehicle = Vehicle.builder()
                .brand("Citroën")
                .model("C3")
                .energy("Essence")
                .mileage(60000)
                .monthlyPrice(new BigDecimal("199"))
                .mode(VehicleMode.RENTAL)
                .available(false)
                .description("Véhicule non disponible")
                .build();

        vehicleRepository.save(saleVehicle);
        vehicleRepository.save(rentalVehicle);
        vehicleRepository.save(unavailableRentalVehicle);

        List<Vehicle> rentalVehicles = vehicleRepository.findByModeAndAvailableTrue(VehicleMode.RENTAL);

        assertThat(rentalVehicles).hasSize(1);
        assertThat(rentalVehicles.get(0).getBrand()).isEqualTo("Renault");
        assertThat(rentalVehicles.get(0).getMode()).isEqualTo(VehicleMode.RENTAL);
        assertThat(rentalVehicles.get(0).getAvailable()).isTrue();
    }
}
