package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.ApplicationFileRequest;
import com.keyd.mmotors.dto.ApplicationFileStatusRequest;
import com.keyd.mmotors.entity.*;
import com.keyd.mmotors.exception.BusinessRuleException;
import com.keyd.mmotors.exception.ResourceNotFoundException;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationFileService {

    private final ApplicationFileRepository applicationFileRepository;
    private final AppUserRepository appUserRepository;
    private final VehicleRepository vehicleRepository;

    public ApplicationFile createApplicationFile(String clientEmail, ApplicationFileRequest request) {
        AppUser client = appUserRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientEmail));

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'identifiant : " + request.vehicleId()));

        validateApplicationTypeWithVehicleMode(request.type(), vehicle.getMode());

        ApplicationFile applicationFile = ApplicationFile.builder()
                .type(request.type())
                .status(ApplicationStatus.SUBMITTED)
                .client(client)
                .vehicle(vehicle)
                .build();

        return applicationFileRepository.save(applicationFile);
    }

    public List<ApplicationFile> findClientApplicationFiles(String clientEmail) {
        AppUser client = appUserRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientEmail));

        return applicationFileRepository.findByClientId(client.getId());
    }

    public ApplicationFile findClientApplicationFileById(String clientEmail, Long applicationFileId) {
        AppUser client = appUserRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientEmail));

        return applicationFileRepository.findByIdAndClientId(applicationFileId, client.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable pour ce client"));
    }

    public List<ApplicationFile> findAllApplicationFiles() {
        return applicationFileRepository.findAll();
    }

    public ApplicationFile updateApplicationFileStatus(Long applicationFileId, ApplicationFileStatusRequest request) {
        ApplicationFile applicationFile = applicationFileRepository.findById(applicationFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec l'identifiant : " + applicationFileId));

        applicationFile.setStatus(request.status());
        applicationFile.setAdminComment(request.adminComment());

        return applicationFileRepository.save(applicationFile);
    }

    private void validateApplicationTypeWithVehicleMode(ApplicationType applicationType, VehicleMode vehicleMode) {
        if (applicationType == ApplicationType.PURCHASE && vehicleMode != VehicleMode.SALE) {
            throw new BusinessRuleException("Un dossier d'achat doit être associé à un véhicule en vente");
        }

        if (applicationType == ApplicationType.RENTAL && vehicleMode != VehicleMode.RENTAL) {
            throw new BusinessRuleException("Un dossier de location doit être associé à un véhicule en location");
        }
    }
}
