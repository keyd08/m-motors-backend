package com.keyd.mmotors.service;

import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import com.keyd.mmotors.exception.ResourceNotFoundException;
import com.keyd.mmotors.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public List<Vehicle> findAvailableVehicles(VehicleMode mode) {
        if (mode == null) {
            return vehicleRepository.findByAvailableTrue();
        }

        return vehicleRepository.findByModeAndAvailableTrue(mode);
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'identifiant : " + id));
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        vehicle.setId(null);

        if (vehicle.getAvailable() == null) {
            vehicle.setAvailable(true);
        }

        return vehicleRepository.save(vehicle);
    }

    public Vehicle switchVehicleMode(Long id, VehicleMode newMode) {
        Vehicle vehicle = findById(id);
        vehicle.setMode(newMode);

        return vehicleRepository.save(vehicle);
    }
}
