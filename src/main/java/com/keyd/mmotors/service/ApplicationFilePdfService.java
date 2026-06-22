package com.keyd.mmotors.service;

import com.keyd.mmotors.entity.ApplicationFile;
import com.keyd.mmotors.entity.ApplicationStatus;
import com.keyd.mmotors.entity.ApplicationType;
import com.keyd.mmotors.entity.DocumentFile;
import com.keyd.mmotors.entity.DocumentType;
import com.keyd.mmotors.entity.Vehicle;
import com.keyd.mmotors.entity.VehicleMode;
import com.keyd.mmotors.exception.BusinessRuleException;
import com.keyd.mmotors.exception.ResourceNotFoundException;
import com.keyd.mmotors.repository.ApplicationFileRepository;
import com.keyd.mmotors.repository.DocumentFileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationFilePdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final ApplicationFileService applicationFileService;
    private final ApplicationFileRepository applicationFileRepository;
    private final DocumentFileRepository documentFileRepository;

    public byte[] generateClientSummaryPdf(String clientEmail, Long applicationFileId) {
        ApplicationFile applicationFile = applicationFileService
                .findClientApplicationFileById(clientEmail, applicationFileId);

        return generateSummary(applicationFile);
    }

    public byte[] generateAdminSummaryPdf(Long applicationFileId) {
        ApplicationFile applicationFile = applicationFileRepository.findById(applicationFileId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier introuvable avec l'identifiant : " + applicationFileId));

        return generateSummary(applicationFile);
    }

    private byte[] generateSummary(ApplicationFile applicationFile) {
        List<DocumentFile> documents = documentFileRepository.findByApplicationFileId(applicationFile.getId());

        try (PDDocument pdf = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            pdf.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
                float y = 790;

                writeLine(content, y, "M-Motors - Récapitulatif de dossier", FONT_BOLD, 18);
                y -= 32;

                writeLine(content, y, "Dossier #" + applicationFile.getId(), FONT_BOLD, 13);
                y -= 20;
                writeLine(content, y, "Statut : " + label(applicationFile.getStatus()), FONT_REGULAR, 11);
                y -= 16;
                writeLine(content, y, "Type : " + label(applicationFile.getType()), FONT_REGULAR, 11);
                y -= 16;
                writeLine(content, y, "Créé le : " + formatDate(applicationFile.getCreatedAt()), FONT_REGULAR, 11);
                y -= 24;

                writeLine(content, y, "Client", FONT_BOLD, 12);
                y -= 18;
                writeLine(content, y, "Nom : " + applicationFile.getClient().getFirstName() + " " + applicationFile.getClient().getLastName(), FONT_REGULAR, 11);
                y -= 16;
                writeLine(content, y, "Email : " + applicationFile.getClient().getEmail(), FONT_REGULAR, 11);
                y -= 24;

                Vehicle vehicle = applicationFile.getVehicle();
                writeLine(content, y, "Véhicule", FONT_BOLD, 12);
                y -= 18;
                writeLine(content, y, vehicle.getBrand() + " " + vehicle.getModel() + " - " + vehicle.getEnergy(), FONT_REGULAR, 11);
                y -= 16;
                writeLine(content, y, "Kilométrage : " + vehicle.getMileage() + " km", FONT_REGULAR, 11);
                y -= 16;
                writeLine(content, y, "Budget : " + formatVehicleBudget(vehicle), FONT_REGULAR, 11);
                y -= 24;

                writeLine(content, y, "Documents transmis", FONT_BOLD, 12);
                y -= 18;

                if (documents.isEmpty()) {
                    writeLine(content, y, "Aucun document transmis.", FONT_REGULAR, 11);
                    y -= 16;
                } else {
                    for (DocumentFile document : documents) {
                        writeLine(
                                content,
                                y,
                                "- " + label(document.getType()) + " : " + document.getFileName() + " (" + formatFileSize(document.getSize()) + ")",
                                FONT_REGULAR,
                                10
                        );
                        y -= 15;
                    }
                }

                if (applicationFile.getAdminComment() != null && !applicationFile.getAdminComment().isBlank()) {
                    y -= 16;
                    writeLine(content, y, "Commentaire administration", FONT_BOLD, 12);
                    y -= 18;
                    writeLine(content, y, applicationFile.getAdminComment(), FONT_REGULAR, 10);
                }
            }

            pdf.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new BusinessRuleException("Impossible de générer le récapitulatif PDF");
        }
    }

    private void writeLine(PDPageContentStream content, float y, String text, PDType1Font font, int fontSize) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(50, y);
        content.showText(truncate(sanitize(text), 120));
        content.endText();
    }

    private String sanitize(String value) {
        if (value == null) {
            return "Non renseigné";
        }

        return value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ')
                .replace('’', '\'');
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "Non renseignée" : value.format(DATE_FORMATTER);
    }

    private String formatVehicleBudget(Vehicle vehicle) {
        if (vehicle.getMode() == VehicleMode.RENTAL) {
            return formatAmount(vehicle.getMonthlyPrice()) + " €/mois";
        }

        return formatAmount(vehicle.getPrice()) + " €";
    }

    private String formatAmount(BigDecimal amount) {
        return amount == null ? "Non renseigné" : amount.stripTrailingZeros().toPlainString();
    }

    private String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "taille non renseignée";
        }

        if (size < 1024 * 1024) {
            return Math.round(size / 1024.0) + " Ko";
        }

        return String.format("%.1f Mo", size / 1024.0 / 1024.0);
    }

    private String label(ApplicationType type) {
        return type == ApplicationType.RENTAL ? "Location longue durée" : "Achat véhicule";
    }

    private String label(ApplicationStatus status) {
        return switch (status) {
            case INCOMPLETE -> "À compléter";
            case SUBMITTED -> "Déposé";
            case IN_PROGRESS -> "En cours";
            case APPROVED -> "Validé";
            case REJECTED -> "Refusé";
        };
    }

    private String label(DocumentType type) {
        return switch (type) {
            case IDENTITY_DOCUMENT -> "Pièce d'identité";
            case PROOF_OF_ADDRESS -> "Justificatif de domicile";
            case PAYSLIP -> "Bulletin de salaire";
            case BANK_DETAILS -> "RIB";
            case OTHER -> "Autre document";
        };
    }
}
