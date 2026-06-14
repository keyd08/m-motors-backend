package com.keyd.mmotors.config;

import com.keyd.mmotors.entity.AppUser;
import com.keyd.mmotors.entity.Role;
import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDemoUsers();
        createDemoVehicles();
    }

    private void createDemoUsers() {
        if (!appUserRepository.existsByEmail("admin@mmotors.demo")) {
            AppUser admin = AppUser.builder()
                    .firstName("Admin")
                    .lastName("M-Motors")
                    .email("admin@mmotors.demo")
                    .password(passwordEncoder.encode("Admin123!"))
                    .role(Role.ADMIN)
                    .build();

            appUserRepository.save(admin);
        }

        if (!appUserRepository.existsByEmail("client@mmotors.demo")) {
            AppUser client = AppUser.builder()
                    .firstName("Client")
                    .lastName("Demo")
                    .email("client@mmotors.demo")
                    .password(passwordEncoder.encode("Client123!"))
                    .role(Role.CLIENT)
                    .build();

            appUserRepository.save(client);
        }
    }

    private void createDemoVehicles() {
        if (vehicleRepository.count() > 0) {
            return;
        }

        List<Vehicle> vehicles = List.of(
                Vehicle.builder()
                        .brand("Peugeot")
                        .model("308")
                        .energy("Diesel")
                        .mileage(85000)
                        .price(new BigDecimal("12900"))
                        .mode(VehicleMode.SALE)
                        .available(true)
                        .description("Véhicule disponible à l'achat")
                        .build(),

                Vehicle.builder()
                        .brand("Renault")
                        .model("Clio")
                        .energy("Essence")
                        .mileage(42000)
                        .monthlyPrice(new BigDecimal("249"))
                        .mode(VehicleMode.RENTAL)
                        .available(true)
                        .description("Véhicule disponible à la location")
                        .build(),

                Vehicle.builder()
                        .brand("Citroën")
                        .model("C3")
                        .energy("Hybride")
                        .mileage(30000)
                        .price(new BigDecimal("14900"))
                        .mode(VehicleMode.SALE)
                        .available(true)
                        .description("Véhicule hybride disponible à l'achat")
                        .build()
        );

        vehicleRepository.saveAll(vehicles);
    }
}
