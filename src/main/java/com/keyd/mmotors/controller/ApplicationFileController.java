package com.keyd.mmotors.controller;

import com.keyd.mmotors.dto.ApplicationFileRequest;
import com.keyd.mmotors.dto.ApplicationFileResponse;
import com.keyd.mmotors.dto.ApplicationFileStatusRequest;
import com.keyd.mmotors.entity.ApplicationFile;
import com.keyd.mmotors.service.ApplicationFileService;
import com.keyd.mmotors.service.ApplicationFilePdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/application-files")
@RequiredArgsConstructor
public class ApplicationFileController {

    private final ApplicationFileService applicationFileService;
    private final ApplicationFilePdfService applicationFilePdfService;

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


    @GetMapping(value = "/my/{id}/summary.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadMySummaryPdf(
            Principal principal,
            @PathVariable Long id
    ) {
        byte[] pdf = applicationFilePdfService.generateClientSummaryPdf(principal.getName(), id);

        return pdfResponse(pdf, "recapitulatif-dossier-" + id + ".pdf");
    }

    @GetMapping(value = "/admin/{id}/summary.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadAdminSummaryPdf(@PathVariable Long id) {
        byte[] pdf = applicationFilePdfService.generateAdminSummaryPdf(id);

        return pdfResponse(pdf, "recapitulatif-dossier-" + id + ".pdf");
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
    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString()
                )
                .body(pdf);
    }

}
