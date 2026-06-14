package com.keyd.mmotors.dto;

import com.keyd.mmotors.entity.ApplicationFile;
import com.keyd.mmotors.entity.ApplicationStatus;
import com.keyd.mmotors.entity.ApplicationType;

import java.time.LocalDateTime;

public record ApplicationFileResponse(
        Long id,
        ApplicationType type,
        ApplicationStatus status,
        String clientEmail,
        Long vehicleId,
        String vehicleBrand,
        String vehicleModel,
        String adminComment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ApplicationFileResponse fromEntity(ApplicationFile applicationFile) {
        return new ApplicationFileResponse(
                applicationFile.getId(),
                applicationFile.getType(),
                applicationFile.getStatus(),
                applicationFile.getClient().getEmail(),
                applicationFile.getVehicle().getId(),
                applicationFile.getVehicle().getBrand(),
                applicationFile.getVehicle().getModel(),
                applicationFile.getAdminComment(),
                applicationFile.getCreatedAt(),
                applicationFile.getUpdatedAt()
        );
    }
}
