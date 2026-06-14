package com.keyd.mmotors.config;

import com.keyd.mmotors.entity.AppUser;
import com.keyd.mmotors.entity.Role;
import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Doit créer les utilisateurs et véhicules de démonstration si les données sont absentes")
    void shouldCreateDemoUsersAndVehiclesWhenDataIsMissing() {
        when(appUserRepository.existsByEmail("admin@mmotors.demo")).thenReturn(false);
        when(appUserRepository.existsByEmail("client@mmotors.demo")).thenReturn(false);
        when(passwordEncoder.encode("Admin123!")).thenReturn("encoded-admin-password");
        when(passwordEncoder.encode("Client123!")).thenReturn("encoded-client-password");
        when(vehicleRepository.count()).thenReturn(0L);

        DataInitializer dataInitializer = new DataInitializer(
                appUserRepository,
                vehicleRepository,
                passwordEncoder
        );

        dataInitializer.run();

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository, times(2)).save(userCaptor.capture());

        List<AppUser> savedUsers = userCaptor.getAllValues();

        assertThat(savedUsers)
                .extracting(AppUser::getEmail)
                .containsExactly("admin@mmotors.demo", "client@mmotors.demo");

        assertThat(savedUsers.get(0).getRole()).isEqualTo(Role.ADMIN);
        assertThat(savedUsers.get(1).getRole()).isEqualTo(Role.CLIENT);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Vehicle>> vehiclesCaptor = ArgumentCaptor.forClass(Iterable.class);

        verify(vehicleRepository).saveAll(vehiclesCaptor.capture());

        List<Vehicle> savedVehicles = StreamSupport
                .stream(vehiclesCaptor.getValue().spliterator(), false)
                .toList();

        assertThat(savedVehicles).hasSize(3);
        assertThat(savedVehicles)
                .extracting(Vehicle::getMode)
                .contains(VehicleMode.SALE, VehicleMode.RENTAL);
    }

    @Test
    @DisplayName("Ne doit pas recréer les données de démonstration si elles existent déjà")
    void shouldNotDuplicateDemoDataWhenDataAlreadyExists() {
        when(appUserRepository.existsByEmail("admin@mmotors.demo")).thenReturn(true);
        when(appUserRepository.existsByEmail("client@mmotors.demo")).thenReturn(true);
        when(vehicleRepository.count()).thenReturn(3L);

        DataInitializer dataInitializer = new DataInitializer(
                appUserRepository,
                vehicleRepository,
                passwordEncoder
        );

        dataInitializer.run();

        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(vehicleRepository, never()).saveAll(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
