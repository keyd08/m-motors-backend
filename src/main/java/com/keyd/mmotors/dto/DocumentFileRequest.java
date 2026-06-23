package com.keyd.mmotors.dto;

import com.keyd.mmotors.entity.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DocumentFileRequest(

        @NotNull(message = "Le type de document est obligatoire")
        DocumentType type,

        @NotBlank(message = "Le nom du fichier est obligatoire")
        String fileName,

        @NotBlank(message = "Le chemin du fichier est obligatoire")
        String filePath,

        @NotBlank(message = "Le type MIME du fichier est obligatoire")
        String contentType,

        @NotNull(message = "La taille du fichier est obligatoire")
        @Positive(message = "La taille du fichier doit être positive")
        Long size
) {
}
