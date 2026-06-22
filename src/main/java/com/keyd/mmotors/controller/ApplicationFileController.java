package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.ApplicationFileRequest;
import com.keyd.mmotors.dto.ApplicationFileResponse;
import com.keyd.mmotors.dto.ApplicationFileStatusRequest;
import com.keyd.mmotors.entity.ApplicationFile;
import com.keyd.mmotors.service.ApplicationFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/application-files")
@RequiredArgsConstructor
public class ApplicationFileController {

    private final ApplicationFileService applicationFileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationFileResponse createApplicationFile(
            Principal principal,
            @Valid @RequestBody ApplicationFileRequest request
    ) {
        ApplicationFile createdApplicationFile = applicationFileService
                .createApplicationFile(principal.getName(), request);

        return ApplicationFileResponse.fromEntity(createdApplicationFile);
    }

    @GetMapping("/my")
    public List<ApplicationFileResponse> findMyApplicationFiles(Principal principal) {
        return applicationFileService.findClientApplicationFiles(principal.getName())
                .stream()
                .map(ApplicationFileResponse::fromEntity)
                .toList();
    }


    @DeleteMapping("/my/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyApplicationFile(
            Principal principal,
            @PathVariable Long id
    ) {
        applicationFileService.deleteClientApplicationFile(principal.getName(), id);
    }

    @GetMapping("/my/{id}")
    public ApplicationFileResponse findMyApplicationFileById(
            Principal principal,
            @PathVariable Long id
    ) {
        ApplicationFile applicationFile = applicationFileService
                .findClientApplicationFileById(principal.getName(), id);

        return ApplicationFileResponse.fromEntity(applicationFile);
    }

    @PatchMapping("/my/{id}/submit")
    public ApplicationFileResponse submitMyApplicationFile(
            Principal principal,
            @PathVariable Long id
    ) {
        ApplicationFile applicationFile = applicationFileService
                .submitClientApplicationFile(principal.getName(), id);

        return ApplicationFileResponse.fromEntity(applicationFile);
    }

    @GetMapping("/admin")
    public List<ApplicationFileResponse> findAllApplicationFiles() {
        return applicationFileService.findAllApplicationFiles()
                .stream()
                .map(ApplicationFileResponse::fromEntity)
                .toList();
    }

    @PatchMapping("/admin/{id}/status")
    public ApplicationFileResponse updateApplicationFileStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationFileStatusRequest request
    ) {
        ApplicationFile updatedApplicationFile = applicationFileService
                .updateApplicationFileStatus(id, request);

        return ApplicationFileResponse.fromEntity(updatedApplicationFile);
    }
}
