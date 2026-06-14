package com.keyd.mmotors.dto;

import com.keyd.mmotors.entity.ApplicationType;
import jakarta.validation.constraints.NotNull;

public record ApplicationFileRequest(

        @NotNull(message = "Le type de dossier est obligatoire")
        ApplicationType type,

        @NotNull(message = "Le véhicule est obligatoire")
        Long vehicleId
) {
}
