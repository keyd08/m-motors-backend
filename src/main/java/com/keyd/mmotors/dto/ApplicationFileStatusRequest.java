package com.keyd.mmotors.dto;

import com.keyd.mmotors.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationFileStatusRequest(

        @NotNull(message = "Le statut est obligatoire")
        ApplicationStatus status,

        String adminComment
) {
}
