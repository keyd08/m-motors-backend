package com.keyd.mmotors.dto;

import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        String brand,
        String model,
        String energy,
        Integer mileage,
        BigDecimal price,
        BigDecimal monthlyPrice,
        VehicleMode mode,
        Boolean available,
        String description,
        LocalDateTime createdAt
) {

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getEnergy(),
                vehicle.getMileage(),
                vehicle.getPrice(),
                vehicle.getMonthlyPrice(),
                vehicle.getMode(),
                vehicle.getAvailable(),
                vehicle.getDescription(),
                vehicle.getCreatedAt()
        );
    }
}
