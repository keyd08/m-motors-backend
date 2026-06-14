package com.keyd.mmotors.dto;

import com.keyd.mmotors.entity.DocumentFile;
import com.keyd.mmotors.entity.DocumentType;

import java.time.LocalDateTime;

public record DocumentFileResponse(
        Long id,
        DocumentType type,
        String fileName,
        String filePath,
        String contentType,
        Long size,
        Long applicationFileId,
        LocalDateTime uploadedAt
) {

    public static DocumentFileResponse fromEntity(DocumentFile documentFile) {
        return new DocumentFileResponse(
                documentFile.getId(),
                documentFile.getType(),
                documentFile.getFileName(),
                documentFile.getFilePath(),
                documentFile.getContentType(),
                documentFile.getSize(),
                documentFile.getApplicationFile().getId(),
                documentFile.getUploadedAt()
        );
    }
}
