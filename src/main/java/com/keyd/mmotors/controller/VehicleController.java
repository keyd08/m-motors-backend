package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.VehicleRequest;
import com.keyd.mmotors.dto.VehicleResponse;
import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import com.keyd.mmotors.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public List<VehicleResponse> findAvailableVehicles(
            @RequestParam(required = false) VehicleMode mode
    ) {
        return vehicleService.findAvailableVehicles(mode)
                .stream()
                .map(VehicleResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public VehicleResponse findVehicleById(@PathVariable Long id) {
        return VehicleResponse.fromEntity(vehicleService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@Valid @RequestBody VehicleRequest request) {
        Vehicle createdVehicle = vehicleService.createVehicle(request.toEntity());
        return VehicleResponse.fromEntity(createdVehicle);
    }

    @PatchMapping("/{id}/mode")
    public VehicleResponse switchVehicleMode(
            @PathVariable Long id,
            @RequestParam VehicleMode mode
    ) {
        Vehicle updatedVehicle = vehicleService.switchVehicleMode(id, mode);
        return VehicleResponse.fromEntity(updatedVehicle);
    }
}
