package com.keyd.mmotors.dto;

import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record VehicleRequest(

        @NotBlank(message = "La marque est obligatoire")
        String brand,

        @NotBlank(message = "Le modèle est obligatoire")
        String model,

        @NotBlank(message = "L'énergie est obligatoire")
        String energy,

        @NotNull(message = "Le kilométrage est obligatoire")
        @PositiveOrZero(message = "Le kilométrage ne peut pas être négatif")
        Integer mileage,

        @PositiveOrZero(message = "Le prix ne peut pas être négatif")
        BigDecimal price,

        @PositiveOrZero(message = "La mensualité ne peut pas être négative")
        BigDecimal monthlyPrice,

        @NotNull(message = "Le mode du véhicule est obligatoire")
        VehicleMode mode,

        Boolean available,

        String description
) {

    public Vehicle toEntity() {
        return Vehicle.builder()
                .brand(brand)
                .model(model)
                .energy(energy)
                .mileage(mileage)
                .price(price)
                .monthlyPrice(monthlyPrice)
                .mode(mode)
                .available(available)
                .description(description)
                .build();
    }
}
