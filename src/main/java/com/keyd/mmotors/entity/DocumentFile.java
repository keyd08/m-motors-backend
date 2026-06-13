package com.keyd.mmotors.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    private String contentType;

    private Long size;

    @ManyToOne(optional = false)
    @JoinColumn(name = "application_file_id")
    private ApplicationFile applicationFile;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
