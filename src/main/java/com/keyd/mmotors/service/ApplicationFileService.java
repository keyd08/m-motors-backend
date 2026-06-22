package com.keyd.mmotors.service;

import com.keyd.mmotors.dto.ApplicationFileRequest;
import com.keyd.mmotors.dto.ApplicationFileStatusRequest;
import com.keyd.mmotors.entity.*;
import com.keyd.mmotors.exception.BusinessRuleException;
import com.keyd.mmotors.exception.ResourceNotFoundException;
import com.keyd.mmotors.repository.AppUserRepository;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.DocumentFileRepository;
import com.keyd.mmotors.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationFileService {

    private final ApplicationFileRepository applicationFileRepository;
    private final AppUserRepository appUserRepository;
    private final VehicleRepository vehicleRepository;
    private final DocumentFileRepository documentFileRepository;

    public ApplicationFile createApplicationFile(String clientEmail, ApplicationFileRequest request) {
        AppUser client = appUserRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientEmail));

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable avec l'identifiant : " + request.vehicleId()));

        validateApplicationTypeWithVehicleMode(request.type(), vehicle.getMode());

        ApplicationFile applicationFile = ApplicationFile.builder()
                .type(request.type())
                .status(ApplicationStatus.INCOMPLETE)
                .client(client)
                .vehicle(vehicle)
                .build();

        return applicationFileRepository.save(applicationFile);
    }


    public void deleteClientApplicationFile(String clientEmail, Long applicationFileId) {
        ApplicationFile applicationFile = findClientApplicationFileById(clientEmail, applicationFileId);

        if (applicationFile.getStatus() != ApplicationStatus.INCOMPLETE) {
            throw new BusinessRuleException("Seul un dossier en cours peut être supprimé");
        }

        List<DocumentFile> documentFiles = documentFileRepository.findByApplicationFileId(applicationFile.getId());
        documentFiles.forEach((documentFile) -> deletePhysicalFile(documentFile.getFilePath()));

        documentFileRepository.deleteByApplicationFileId(applicationFile.getId());
        applicationFileRepository.delete(applicationFile);
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

    public ApplicationFile submitClientApplicationFile(String clientEmail, Long applicationFileId) {
        ApplicationFile applicationFile = findClientApplicationFileById(clientEmail, applicationFileId);

        if (documentFileRepository.countByApplicationFileId(applicationFile.getId()) == 0) {
            throw new BusinessRuleException("Le dossier doit contenir au moins un document avant envoi");
        }

        applicationFile.setStatus(ApplicationStatus.SUBMITTED);
        applicationFile.setAdminComment(null);

        return applicationFileRepository.save(applicationFile);
    }

    public ApplicationFile updateApplicationFileStatus(Long applicationFileId, ApplicationFileStatusRequest request) {
        ApplicationFile applicationFile = applicationFileRepository.findById(applicationFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec l'identifiant : " + applicationFileId));

        applicationFile.setStatus(request.status());
        applicationFile.setAdminComment(request.adminComment());

        return applicationFileRepository.save(applicationFile);
    }


    private void deletePhysicalFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath).toAbsolutePath().normalize());
        } catch (IOException exception) {
            throw new BusinessRuleException("Impossible de supprimer un document du dossier");
        }
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
