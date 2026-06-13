package com.keyd.mmotors.repository;

import com.keyd.mmotors.entity.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {

    List<DocumentFile> findByApplicationFileId(Long applicationFileId);
}
